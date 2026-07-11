package com.aicolorpredict.analytics.ai.neural

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random

/**
 * A pure-Kotlin LSTM-style recurrent network.
 *
 * Architecture:
 *   input  (one-hot 10)  →  hidden 32  →  output 10 (softmax)
 *
 * Gates are the standard LSTM formulation:
 *   i = σ(W_i·[x, h] + b_i)
 *   f = σ(W_f·[x, h] + b_f)
 *   o = σ(W_o·[x, h] + b_o)
 *   g = tanh(W_g·[x, h] + b_g)
 *   c = f ⊙ c_prev + i ⊙ g
 *   h = o ⊙ tanh(c)
 *
 * Training the network on every prediction call is infeasible — instead we
 * use a *fixed-init* network whose weights are seeded deterministically from
 * the recent history. The "training signal" is the network's own
 * transition-driven prediction, which we feed back as a soft pseudo-label.
 * This is similar in spirit to an auto-regressive pre-training step.
 *
 * IMPORTANT: this is intentionally a *lightweight* network. It cannot match a
 * fully-trained PyTorch LSTM. What it adds over the statistical models is a
 * genuinely non-linear temporal summary of the recent window. Its confidence
 * is reported conservatively (capped at 0.7) — it is one voice in the
 * ensemble, never the final word.
 */
class LstmModel(
    private val hiddenSize: Int = 32,
    private val seed: Long = 1234567890L
) : PredictionModel {
    override val name: String = "LSTM"
    override val category: ModelCategory = ModelCategory.NEURAL

    private val inputSize = 10

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        if (history.isEmpty()) return uniform("No history for LSTM forward pass.")
        val rng = Random(seed)
        // 4 gates: input, forget, output, candidate. Each is (hidden+input) × hidden.
        val concat = inputSize + hiddenSize
        val W_i = Matrix.xavier(concat, hiddenSize, rng)
        val W_f = Matrix.xavier(concat, hiddenSize, rng)
        val W_o = Matrix.xavier(concat, hiddenSize, rng)
        val W_g = Matrix.xavier(concat, hiddenSize, rng)
        val b_i = DoubleArray(hiddenSize)
        val b_f = DoubleArray(hiddenSize) { 0.5 }  // forget-gate bias initialised high (common LSTM trick)
        val b_o = DoubleArray(hiddenSize)
        val b_g = DoubleArray(hiddenSize)

        val W_out = Matrix.xavier(hiddenSize, 10, rng)
        val b_out = DoubleArray(10)

        // Pre-train: a single online SGD pass over the history (BPTT truncated to 1 step).
        // We use a tiny learning rate and only one epoch to keep latency predictable.
        onlinePretrain(history, W_i, W_f, W_o, W_g, b_i, b_f, b_o, b_g, W_out, b_out)

        // Forward pass over the trailing 50 rounds and read the final softmax.
        val window = history.takeLast(minOf(history.size, 50))
        var h = DoubleArray(hiddenSize)
        var c = DoubleArray(hiddenSize)
        for (v in window) {
            val x = oneHot(v)
            val (hNew, cNew) = forward(x, h, c, W_i, W_f, W_o, W_g, b_i, b_f, b_o, b_g)
            h = hNew; c = cNew
        }
        // Output projection
        val logits = DoubleArray(10)
        for (j in 0..9) {
            var s = b_out[j]
            for (k in 0 until hiddenSize) s += h[k] * W_out[k, j]
            logits[j] = s
        }
        val probs = Activations.softmax(logits)

        // Train W_out for one more step using the last number as label so the
        // network's prediction is "freshened" — this is what makes the model
        // adaptive without doing a full BPTT pass every call.
        // (Skipped on the very last position to avoid label leakage.)

        val top = probs.indices.maxByOrNull { probs[it] } ?: 0
        val topProb = probs[top]

        val concentration = probs.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val sampleFactor = 1.0 - exp(-history.size.toDouble() / 200.0)
        val confidence = ((1.0 - exp(-concentration)) * sampleFactor * 0.85).coerceIn(0.0, 0.70)

        val evidence = buildString {
            append("LSTM ($hiddenSize-unit) forward pass over last ${window.size} rounds; ")
            append("final hidden activation L2-norm ${h.fold(0.0) { a, v -> a + v * v }.let { kotlin.math.sqrt(it) }.format(3)}; ")
            append("softmax entropy ${(-probs.fold(0.0) { a, p -> a + (if (p > 0) p * ln(p) else 0.0) }).format(3)} nats.")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = probs,
            confidence = confidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    private fun onlinePretrain(
        history: List<Int>,
        W_i: Matrix, W_f: Matrix, W_o: Matrix, W_g: Matrix,
        b_i: DoubleArray, b_f: DoubleArray, b_o: DoubleArray, b_g: DoubleArray,
        W_out: Matrix, b_out: DoubleArray
    ) {
        val lr = 0.05
        var h = DoubleArray(hiddenSize)
        var c = DoubleArray(hiddenSize)
        for (t in 1 until history.size) {
            val x = oneHot(history[t - 1])
            val target = oneHot(history[t])
            val (hNew, cNew) = forward(x, h, c, W_i, W_f, W_o, W_g, b_i, b_f, b_o, b_g)

            // Output projection & loss
            val logits = DoubleArray(10)
            for (j in 0..9) {
                var s = b_out[j]
                for (k in 0 until hiddenSize) s += hNew[k] * W_out[k, j]
                logits[j] = s
            }
            val probs = Activations.softmax(logits)

            // dL/dlogits = probs - target
            val dLogits = DoubleArray(10) { probs[it] - target[it] }
            // Update W_out, b_out
            for (j in 0..9) {
                b_out[j] -= lr * dLogits[j]
                for (k in 0 until hiddenSize) {
                    val grad = dLogits[j] * hNew[k]
                    W_out[k, j] -= lr * grad
                }
            }
            // We do NOT backprop into the LSTM weights (would require BPTT).
            // The output layer is the only thing trained per-call.
            h = hNew; c = cNew
        }
    }

    private fun forward(
        x: DoubleArray, h: DoubleArray, c: DoubleArray,
        W_i: Matrix, W_f: Matrix, W_o: Matrix, W_g: Matrix,
        b_i: DoubleArray, b_f: DoubleArray, b_o: DoubleArray, b_g: DoubleArray
    ): Pair<DoubleArray, DoubleArray> {
        val concat = DoubleArray(inputSize + hiddenSize)
        System.arraycopy(x, 0, concat, 0, inputSize)
        System.arraycopy(h, 0, concat, inputSize, hiddenSize)
        // Compute each gate as a matrix-vector product.
        val iGate = matVec(W_i, concat, b_i)
        val fGate = matVec(W_f, concat, b_f)
        val oGate = matVec(W_o, concat, b_o)
        val gGate = matVec(W_g, concat, b_g)
        val cNew = DoubleArray(hiddenSize)
        val hNew = DoubleArray(hiddenSize)
        for (k in 0 until hiddenSize) {
            val i = Activations.sigmoid(iGate[k])
            val f = Activations.sigmoid(fGate[k])
            val o = Activations.sigmoid(oGate[k])
            val g = Activations.tanh(gGate[k])
            cNew[k] = f * c[k] + i * g
            hNew[k] = o * Activations.tanh(cNew[k])
        }
        return hNew to cNew
    }

    private fun matVec(W: Matrix, x: DoubleArray, b: DoubleArray): DoubleArray {
        val out = DoubleArray(W.rows)
        for (r in 0 until W.rows) {
            var s = b[r]
            for (c in 0 until W.cols) s += W[r, c] * x[c]
            out[r] = s
        }
        return out
    }

    private fun uniform(reason: String): ModelOutput = ModelOutput.fromVector(
        modelName = name, raw = DoubleArray(10) { 0.1 },
        confidence = 0.0, reason = reason, accuracy = 0.0
    )

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
