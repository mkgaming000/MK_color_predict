package com.aicolorpredict.analytics.ai.base

import com.aicolorpredict.analytics.domain.model.Confidence

/**
 * Helpers shared by every model — explanation generators, smoothing,
 * confidence-from-probability, etc.
 */
object ModelUtils {

    /** Laplace-smoothed probability vector from counts. */
    fun laplace(counts: IntArray, alpha: Double = 1.0): DoubleArray {
        val total = counts.sumOf { it.toLong() }.toDouble() + alpha * counts.size
        if (total <= 0) return DoubleArray(counts.size) { 1.0 / counts.size }
        return DoubleArray(counts.size) { (counts[it] + alpha) / total }
    }

    /** Mix two probability vectors with a weight (0..1) on the first. */
    fun mix(a: DoubleArray, b: DoubleArray, weightOnA: Double): DoubleArray {
        require(a.size == b.size)
        val w = weightOnA.coerceIn(0.0, 1.0)
        val out = DoubleArray(a.size)
        for (i in out.indices) out[i] = w * a[i] + (1 - w) * b[i]
        return normalise(out)
    }

    fun normalise(v: DoubleArray): DoubleArray {
        val sum = v.sum()
        if (sum <= 0) return DoubleArray(v.size) { 1.0 / v.size }
        return DoubleArray(v.size) { v[it] / sum }
    }

    /** Confidence in [0,1] from a top probability, using a saturating function. */
    fun confidenceFromTop(top: Double, baseline: Double = 0.1): Double {
        val excess = (top - baseline).coerceIn(0.0, 1.0)
        val scaled = 6.0 * (excess / (1.0 - baseline).coerceAtLeast(1e-6))
        return (1.0 / (1.0 + kotlin.math.exp(-scaled))).coerceIn(0.0, 1.0)
    }

    fun reasonTopPick(name: String, top: Int, prob: Double, evidence: String): String =
        "$name favours number $top (${"%.${1}f".format(prob * 100)}%). $evidence"
}

/**
 * Helper for models that need to express their confidence in a human-readable
 * band. Internally uses [Confidence.fromProbability] but adds the consensus
 * estimate from the ensemble.
 */
fun modelConfidenceLabel(top: Double, consensus: com.aicolorpredict.analytics.domain.model.ConsensusLevel): String =
    Confidence.fromProbability(top, consensus).label
