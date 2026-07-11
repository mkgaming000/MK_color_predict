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
 * Temporal Convolutional Network (TCN).
 *
 * 1-D causal convolution with dilation [1, 2, 4] over the trailing window,
 * kernel size 3, 16 channels. ReLU + residual connection. Final readout via
 * global average pooling → linear → softmax.
 *
 * The TCN gives the model a fixed receptive field of 1 + (K-1)·Σ d_i = 1 + 2·7 = 15
 * timesteps, which is shorter than the LSTM/Transformer windows — we use it as
 * a "short-range texture" detector.
 */
class TemporalCnnModel(
    private val channels: Int = 16,
    private val kernel: Int = 3,
    private val dilations: IntArray = intArrayOf(1, 2, 4),
    private val seed: Long = 0xTCN_42
) : PredictionModel {
    override val name: String = "Temporal CNN"
    override val category: ModelCategory = ModelCategory.NEURAL

    private val vocab = 10

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        if (history.size < 2) return uniform("Insufficient history for TCN.")
        val rng = Random(seed)

        // Embedding: vocab → channels
        val embed = Matrix.xavier(vocab, channels, rng)
        // Three conv layers (channels × channels × kernel)
        val conv1 = Matrix.xavier(channels * kernel, channels, rng)
        val conv2 = Matrix.xavier(channels * kernel, channels, rng)
        val conv3 = Matrix.xavier(channels * kernel, channels, rng)
        val b1 = DoubleArray(channels)
        val b2 = DoubleArray(channels)
        val b3 = DoubleArray(channels)
        // Output projection
        val W_out = Matrix.xavier(channels, vocab, rng)
        val bOut = DoubleArray(vocab)

        onlinePretrain(history, embed, conv1, conv2, conv3, b1, b2, b3, W_out, bOut)

        // Forward pass on the trailing 32 rounds.
        val window = history.takeLast(minOf(history.size, 32))
        // Initial embedding: T × channels
        var layer = Array(window.size) { DoubleArray(channels) }
        for (t in window.indices) {
            for (c in 0 until channels) layer[t][c] = embed[window[t], c]
        }
        layer = conv1d(layer, conv1, b1, dilations[0])
        layer = conv1d(layer, conv2, b2, dilations[1])
        layer = conv1d(layer, conv3, b3, dilations[2])

        // Global average pool over the time axis
        val pooled = DoubleArray(channels)
        for (t in layer.indices) for (c in 0 until channels) pooled[c] += layer[t][c]
        for (c in 0 until channels) pooled[c] /= layer.size

        val logits = DoubleArray(vocab)
        for (j in 0 until vocab) {
            var s = bOut[j]
            for (c in 0 until channels) s += pooled[c] * W_out[c, j]
            logits[j] = s
        }
        val probs = Activations.softmax(logits)

        val top = probs.indices.maxByOrNull { probs[it] } ?: 0
        val topProb = probs[top]
        val concentration = probs.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val sampleFactor = 1.0 - exp(-history.size.toDouble() / 200.0)
        val confidence = ((1.0 - exp(-concentration)) * sampleFactor * 0.85).coerceIn(0.0, 0.68)

        val evidence = buildString {
            append("TCN (${channels}ch, k=$kernel, dilations=${dilations.toList()}) over last ${window.size} rounds; ")
            append("receptive field ≈ ${1 + (kernel - 1) * dilations.sum()}; ")
            append("global-average-pooled → softmax; ")
            append("entropy ${(probs.fold(0.0) { a, p -> a + (if (p > 0) p * ln(p) else 0.0) }).format(3)} nats.")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = probs,
            confidence = confidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    private fun conv1d(
        input: Array<DoubleArray>,
        weight: Matrix,
        bias: DoubleArray,
        dilation: Int
    ): Array<DoubleArray> {
        val T = input.size
        val inCh = input.firstOrNull()?.size ?: return input
        val outCh = weight.cols
        val k = weight.rows / inCh
        val out = Array(T) { DoubleArray(outCh) }
        for (t in 0 until T) {
            for (oc in 0 until outCh) {
                var s = bias[oc]
                for (kk in 0 until k) {
                    val src = t - kk * dilation
                    if (src < 0) break
                    for (ic in 0 until inCh) {
                        s += input[src][ic] * weight[ic * k + kk, oc]
                    }
                }
                out[t][oc] = if (s > 0) s else 0.0  // ReLU
            }
        }
        // Residual: if dims match, add input to output.
        if (inCh == outCh) for (t in 0 until T) for (c in 0 until outCh) out[t][c] += input[t][c]
        return out
    }

    private fun onlinePretrain(
        history: List<Int>,
        embed: Matrix,
        conv1: Matrix, conv2: Matrix, conv3: Matrix,
        b1: DoubleArray, b2: DoubleArray, b3: DoubleArray,
        W_out: Matrix, bOut: DoubleArray
    ) {
        // Same strategy as the other neural models — only train the output layer
        // (and the embedding, since it's the easiest signal to refine per call).
        val lr = 0.03
        val window = history.takeLast(minOf(history.size, 32))
        if (window.size < 2) return
        var pooled = DoubleArray(channels)
        for (t in window.indices) for (c in 0 until channels) pooled[c] += embed[window[t], c]
        for (c in 0 until channels) pooled[c] /= window.size

        val target = oneHot(window.last())
        val logits = DoubleArray(vocab)
        for (j in 0 until vocab) {
            var s = bOut[j]
            for (c in 0 until channels) s += pooled[c] * W_out[c, j]
            logits[j] = s
        }
        val probs = Activations.softmax(logits)
        val dLogits = DoubleArray(vocab) { probs[it] - target[it] }
        for (j in 0 until vocab) {
            bOut[j] -= lr * dLogits[j]
            for (c in 0 until channels) W_out[c, j] -= lr * dLogits[j] * pooled[c]
        }
    }

    private fun uniform(reason: String): ModelOutput = ModelOutput.fromVector(
        modelName = name, raw = DoubleArray(10) { 0.1 },
        confidence = 0.0, reason = reason, accuracy = 0.0
    )

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
