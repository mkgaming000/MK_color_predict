package com.aicolorpredict.analytics.ai.neural

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * A toy Transformer-style attention model.
 *
 * Architecture:
 *   - Linear input projection: 10 → dModel (16)
 *   - Single-head self-attention over the last L=32 positions
 *   - Feed-forward: 2× dModel with ReLU
 *   - Linear output projection: dModel → 10 (softmax over the LAST position)
 *
 * Same training strategy as LSTM/GRU: deterministic Xavier init from history,
 * online SGD on the output layer only. Confidence is capped conservatively
 * (0.68) — the Transformer's expressive power on this tiny window is not
 * enough to justify a higher ceiling.
 */
class TransformerModel(
    private val dModel: Int = 16,
    private val seqLen: Int = 32,
    private val seed: Long = 3456789012L
) : PredictionModel {
    override val name: String = "Transformer"
    override val category: ModelCategory = ModelCategory.NEURAL

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        if (history.size < 2) return uniform("Insufficient history for Transformer.")
        val rng = Random(seed)
        val vocab = 10

        // Input embedding (tied with output projection for simplicity).
        val embed = Matrix.xavier(vocab, dModel, rng)
        // Q, K, V projections (all dModel × dModel).
        val Wq = Matrix.xavier(dModel, dModel, rng)
        val Wk = Matrix.xavier(dModel, dModel, rng)
        val Wv = Matrix.xavier(dModel, dModel, rng)
        val bQkv = DoubleArray(dModel)
        // FFN
        val W1 = Matrix.xavier(dModel, 2 * dModel, rng)
        val b1 = DoubleArray(2 * dModel)
        val W2 = Matrix.xavier(2 * dModel, dModel, rng)
        val b2 = DoubleArray(dModel)
        // Output projection (tied with embedding transpose).
        val bOut = DoubleArray(vocab)

        // Online pretrain: one pass, output layer only.
        onlinePretrain(history, embed, Wq, Wk, Wv, bQkv, W1, b1, W2, b2, bOut)

        // Forward pass on the trailing window.
        val window = history.takeLast(minOf(history.size, seqLen))
        val seq = Matrix(window.size, dModel)
        for (i in window.indices) {
            val oh = oneHot(window[i])
            for (j in 0 until dModel) {
                var s = 0.0
                for (k in 0 until vocab) s += oh[k] * embed[k, j]
                seq[i, j] = s
            }
        }
        val q = seq.matmul(Wq).addBroadcastRow(bQkv)
        val k = seq.matmul(Wk).addBroadcastRow(bQkv)
        val v = seq.matmul(Wv).addBroadcastRow(bQkv)

        // Scaled dot-product self-attention (causal mask)
        val scale = 1.0 / sqrt(dModel.toDouble())
        val attended = Matrix(window.size, dModel)
        for (i in 0 until window.size) {
            val weights = DoubleArray(i + 1)
            var max = Double.NEGATIVE_INFINITY
            for (j in 0..i) {
                var s = 0.0
                for (dim in 0 until dModel) s += q[i, dim] * k[j, dim]
                weights[j] = s * scale
                if (weights[j] > max) max = weights[j]
            }
            var sum = 0.0
            for (j in 0..i) { weights[j] = exp(weights[j] - max); sum += weights[j] }
            if (sum > 0) for (j in 0..i) weights[j] /= sum
            for (kk in 0 until dModel) {
                var s = 0.0
                for (j in 0..i) s += weights[j] * v[j, kk]
                attended[i, kk] = s
            }
        }
        // FFN on the last position only (we only need the final hidden state).
        val last = DoubleArray(dModel) { kk -> attended[window.size - 1, kk] }
        val hidden = DoubleArray(dModel)
        for (j in 0 until 2 * dModel) {
            var pre = b1[j]
            for (kk in 0 until dModel) pre += last[kk] * W1[kk, j]
            val act = if (pre > 0) pre else 0.0  // ReLU
            for (kk in 0 until dModel) hidden[kk] += act * W2[j, kk]
        }
        for (kk in 0 until dModel) hidden[kk] += b2[kk]

        // Output projection (tied with embedding)
        val logits = DoubleArray(vocab)
        for (j in 0 until vocab) {
            var s = bOut[j]
            for (kk in 0 until dModel) s += hidden[kk] * embed[j, kk]
            logits[j] = s
        }
        val probs = Activations.softmax(logits)

        val top = probs.indices.maxByOrNull { probs[it] } ?: 0
        val topProb = probs[top]
        val concentration = probs.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val sampleFactor = 1.0 - exp(-history.size.toDouble() / 300.0)
        val confidence = ((1.0 - exp(-concentration)) * sampleFactor * 0.85).coerceIn(0.0, 0.68)

        val evidence = buildString {
            append("Transformer (d=$dModel, L=$seqLen) attended over last ${window.size} positions; ")
            append("attention entropy on last position ${(probs.fold(0.0) { a, p -> a + (if (p > 0) p * ln(p) else 0.0) }).format(3)} nats; ")
            append("output projection tied with embedding.")
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
        embed: Matrix,
        Wq: Matrix, Wk: Matrix, Wv: Matrix, bQkv: DoubleArray,
        W1: Matrix, b1: DoubleArray, W2: Matrix, b2: DoubleArray,
        bOut: DoubleArray
    ) {
        val lr = 0.03
        // Tiny BPTT: we only train the output bias and the embedding (which is
        // tied to the output projection). This keeps per-call latency predictable.
        val window = history.takeLast(minOf(history.size, seqLen))
        if (window.size < 2) return
        var h = DoubleArray(dModel)
        for (t in 1 until window.size) {
            val target = oneHot(window[t])
            // Crude: use the embedding row of the previous token as "hidden".
            val prevRow = (0 until dModel).map { embed[window[t - 1], it] }.toDoubleArray()
            // Forward output projection.
            val logits = DoubleArray(10)
            for (j in 0 until 10) {
                var s = bOut[j]
                for (kk in 0 until dModel) s += prevRow[kk] * embed[j, kk]
                logits[j] = s
            }
            val probs = Activations.softmax(logits)
            val dLogits = DoubleArray(10) { probs[it] - target[it] }
            for (j in 0 until 10) {
                bOut[j] -= lr * dLogits[j]
                for (kk in 0 until dModel) {
                    val grad = dLogits[j] * prevRow[kk]
                    embed[j, kk] -= lr * grad
                }
            }
            h = prevRow
        }
    }

    private fun uniform(reason: String): ModelOutput = ModelOutput.fromVector(
        modelName = name, raw = DoubleArray(10) { 0.1 },
        confidence = 0.0, reason = reason, accuracy = 0.0
    )

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
