package com.aicolorpredict.analytics.feature

import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Statistical primitives used by the feature engineer and the AI models.
 *
 * Everything here is pure-function so it is trivially unit-testable.
 */
object StatsCalculator {

    fun mean(xs: DoubleArray): Double = if (xs.isEmpty()) 0.0 else xs.sum() / xs.size

    fun variance(xs: DoubleArray): Double {
        if (xs.isEmpty()) return 0.0
        val m = mean(xs)
        return xs.fold(0.0) { acc, x -> acc + (x - m) * (x - m) } / xs.size
    }

    fun std(xs: DoubleArray): Double = sqrt(variance(xs))

    /**
     * Shannon entropy in nats (ln base) for a discrete distribution p.
     * 0 = perfectly deterministic, ln(10) ≈ 2.3026 = uniform over 10 numbers.
     */
    fun entropy(p: DoubleArray): Double {
        var h = 0.0
        for (v in p) if (v > 0.0) h -= v * ln(v)
        return h
    }

    /** Entropy computed from integer counts — adds Laplace smoothing of 1. */
    fun entropyFromCounts(counts: IntArray): Double {
        val total = counts.sumOf { it.toLong() }.toDouble() + counts.size
        if (total <= 0) return 0.0
        var h = 0.0
        for (c in counts) {
            val p = (c + 1.0) / total
            h -= p * ln(p)
        }
        return h
    }

    /** Pearson correlation between two equal-length arrays. */
    fun correlation(a: DoubleArray, b: DoubleArray): Double {
        require(a.size == b.size)
        val n = a.size
        if (n < 2) return 0.0
        val ma = mean(a); val mb = mean(b)
        var num = 0.0; var da = 0.0; var db = 0.0
        for (i in 0 until n) {
            val xa = a[i] - ma; val xb = b[i] - mb
            num += xa * xb; da += xa * xa; db += xb * xb
        }
        if (da <= 0.0 || db <= 0.0) return 0.0
        return num / sqrt(da * db)
    }

    /**
     * Cosine similarity between two equal-length vectors. Used by the historical
     * similarity feature.
     */
    fun cosine(a: DoubleArray, b: DoubleArray): Double {
        require(a.size == b.size)
        var dot = 0.0; var na = 0.0; var nb = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            na += a[i] * a[i]
            nb += b[i] * b[i]
        }
        if (na <= 0.0 || nb <= 0.0) return 0.0
        return dot / (sqrt(na) * sqrt(nb))
    }

    /**
     * Run-length encoding of an integer sequence. Returns the list of (value, length)
     * pairs in encounter order. Useful for streak detection.
     */
    fun <T> runLengthEncode(seq: List<T>): List<Pair<T, Int>> {
        if (seq.isEmpty()) return emptyList()
        val out = mutableListOf<Pair<T, Int>>()
        var cur = seq.first(); var run = 1
        for (i in 1 until seq.size) {
            if (seq[i] == cur) run++
            else { out += cur to run; cur = seq[i]; run = 1 }
        }
        out += cur to run
        return out
    }

    /** Weighted moving average with linearly decaying weights (newer = bigger). */
    fun linearlyWeightedAverage(xs: DoubleArray): Double {
        if (xs.isEmpty()) return 0.0
        var num = 0.0; var den = 0.0
        for (i in xs.indices) {
            val w = (i + 1).toDouble()
            num += xs[i] * w; den += w
        }
        return if (den <= 0.0) 0.0 else num / den
    }
}
