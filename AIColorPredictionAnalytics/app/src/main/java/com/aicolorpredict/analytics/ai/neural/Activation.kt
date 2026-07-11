package com.aicolorpredict.analytics.ai.neural

import kotlin.math.exp
import kotlin.math.tanh

/**
 * Activation functions used by the neural models. Pulled into a single file
 * so we have one place to swap implementations (e.g. approximations for
 * speed) if profiling ever demands it.
 */
object Activations {
    fun sigmoid(x: Double): Double = when {
        x > 30.0 -> 1.0
        x < -30.0 -> 0.0
        else -> 1.0 / (1.0 + exp(-x))
    }

    fun tanh(x: Double): Double = when {
        x > 20.0 -> 1.0
        x < -20.0 -> -1.0
        else -> kotlin.math.tanh(x)
    }

    fun relu(x: Double): Double = if (x > 0) x else 0.0

    fun leakyRelu(x: Double, alpha: Double = 0.01): Double = if (x > 0) x else alpha * x

    fun softmax(x: DoubleArray): DoubleArray {
        val out = DoubleArray(x.size)
        var max = Double.NEGATIVE_INFINITY
        for (v in x) if (v > max) max = v
        var sum = 0.0
        for (i in x.indices) { out[i] = exp(x[i] - max); sum += out[i] }
        if (sum > 0) for (i in x.indices) out[i] /= sum
        else out.fill(1.0 / x.size)
        return out
    }
}

/**
 * One-hot encoding helper for the 10-class number vocabulary.
 */
fun oneHot(label: Int, vocab: Int = 10): DoubleArray {
    val v = DoubleArray(vocab)
    if (label in 0 until vocab) v[label] = 1.0
    return v
}
