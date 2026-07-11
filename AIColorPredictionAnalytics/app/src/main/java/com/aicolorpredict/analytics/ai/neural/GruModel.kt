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
 * Gated Recurrent Unit (GRU) — Cho et al. 2014.
 *
 * Simpler than LSTM (2 gates instead of 3, no separate cell state), and on
 * this size of problem trains a bit faster. Same overall design as the LSTM
 * model: deterministic init from history, online pretrain of the output layer
 * only, conservative confidence cap.
 *
 *   z = σ(W_z·[x, h] + b_z)        (update gate)
 *   r = σ(W_r·[x, h] + b_r)        (reset gate)
 *   h̃ = tanh(W_h·[x, (r ⊙ h)] + b_h)
 *   h = (1 − z) ⊙ h + z ⊙ h̃
 */
class GruModel(
    private val hiddenSize: Int = 32,
    private val seed: Long = 2345678901L
) : PredictionModel {
    override val name: String = "GRU"
    override val category: ModelCategory = ModelCategory.NEURAL

    private val inputSize = 10

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        if (history.isEmpty()) return uniform("No history for GRU forward pass.")
        val rng = Random(seed)
        val concat = inputSize + hiddenSize
        val W_z = Matrix.xavier(concat, hiddenSize, rng)
        val W_r = Matrix.xavier(concat, hiddenSize, rng)
        val W_h = Matrix.xavier(concat, hiddenSize, rng)
        val b_z = DoubleArray(hiddenSize)
        val b_r = DoubleArray(hiddenSize)
        val b_h = DoubleArray(hiddenSize)
        val W_out = Matrix.xavier(hiddenSize, 10, rng)
        val b_out = DoubleArray(10)

        onlinePretrain(history, W_z, W_r, W_h, b_z, b_r, b_h, W_out, b_out)

        val window = history.takeLast(minOf(history.size, 50))
        var h = DoubleArray(hiddenSize)
        for (v in window) {
            val x = oneHot(v)
            h = forward(x, h, W_z, W_r, W_h, b_z, b_r, b_h)
        }
        val logits = DoubleArray(10)
        for (j in 0..9) {
            var s = b_out[j]
            for (k in 0 until hiddenSize) s += h[k] * W_out[k, j]
            logits[j] = s
        }
        val probs = Activations.softmax(logits)

        val top = probs.indices.maxByOrNull { probs[it] } ?: 0
        val topProb = probs[top]
        val concentration = probs.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val sampleFactor = 1.0 - exp(-history.size.toDouble() / 200.0)
        val confidence = ((1.0 - exp(-concentration)) * sampleFactor * 0.85).coerceIn(0.0, 0.70)

        val evidence = buildString {
            append("GRU ($hiddenSize-unit) forward pass over last ${window.size} rounds; ")
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
        W_z: Matrix, W_r: Matrix, W_h: Matrix,
        b_z: DoubleArray, b_r: DoubleArray, b_h: DoubleArray,
        W_out: Matrix, b_out: DoubleArray
    ) {
        val lr = 0.05
        var h = DoubleArray(hiddenSize)
        for (t in 1 until history.size) {
            val x = oneHot(history[t - 1])
            val target = oneHot(history[t])
            val hNew = forward(x, h, W_z, W_r, W_h, b_z, b_r, b_h)
            val logits = DoubleArray(10)
            for (j in 0..9) {
                var s = b_out[j]
                for (k in 0 until hiddenSize) s += hNew[k] * W_out[k, j]
                logits[j] = s
            }
            val probs = Activations.softmax(logits)
            val dLogits = DoubleArray(10) { probs[it] - target[it] }
            for (j in 0..9) {
                b_out[j] -= lr * dLogits[j]
                for (k in 0 until hiddenSize) W_out[k, j] -= lr * dLogits[j] * hNew[k]
            }
            h = hNew
        }
    }

    private fun forward(
        x: DoubleArray, h: DoubleArray,
        W_z: Matrix, W_r: Matrix, W_h: Matrix,
        b_z: DoubleArray, b_r: DoubleArray, b_h: DoubleArray
    ): DoubleArray {
        val concat = DoubleArray(inputSize + hiddenSize)
        System.arraycopy(x, 0, concat, 0, inputSize)
        System.arraycopy(h, 0, concat, inputSize, hiddenSize)
        val z = matVec(W_z, concat, b_z)
        val r = matVec(W_r, concat, b_r)
        val concatRh = DoubleArray(inputSize + hiddenSize)
        System.arraycopy(x, 0, concatRh, 0, inputSize)
        for (k in 0 until hiddenSize) concatRh[inputSize + k] = r[k] * h[k]
        val hTildeRaw = matVec(W_h, concatRh, b_h)
        val hNew = DoubleArray(hiddenSize)
        for (k in 0 until hiddenSize) {
            val zg = Activations.sigmoid(z[k])
            val hTilde = Activations.tanh(hTildeRaw[k])
            hNew[k] = (1.0 - zg) * h[k] + zg * hTilde
        }
        return hNew
    }

    private fun matVec(W: Matrix, x: DoubleArray, b: DoubleArray): DoubleArray {
        // W has dimensions (inputDim × outputDim) where inputDim = W.rows, outputDim = W.cols.
        // output[j] = b[j] + sum_i(W[i, j] * x[i])  for j in 0 until W.cols
        val out = DoubleArray(W.cols)
        for (j in 0 until W.cols) {
            var s = b[j]
            for (i in 0 until W.rows) s += W[i, j] * x[i]
            out[j] = s
        }
        return out
    }

    private fun uniform(reason: String): ModelOutput = ModelOutput.fromVector(
        modelName = name, raw = DoubleArray(10) { 0.1 },
        confidence = 0.0, reason = reason, accuracy = 0.0
    )

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
