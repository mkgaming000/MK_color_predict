package com.aicolorpredict.analytics.ai.calibration

import kotlin.math.exp

/**
 * Platt scaling: fits a logistic map `σ(A·x + B)` that converts a model's raw
 * confidence into a calibrated probability. We use online logistic regression
 * with a tiny learning rate, updated after every resolved prediction.
 *
 * One calibrator is kept per model so calibration is per-model.
 */
class PlattCalibrator(private val lr: Double = 0.05) {
    private var a: Double = 1.0
    private var b: Double = 0.0
    private var n: Int = 0

    /** Apply current calibration. */
    fun calibrate(x: Double): Double {
        val z = a * x + b
        return when {
            z > 30.0 -> 1.0
            z < -30.0 -> 0.0
            else -> 1.0 / (1.0 + exp(-z))
        }
    }

    /**
     * Online update from a single (raw_confidence, was_correct) observation.
     * `wasCorrect` should be 1.0 for a top-1 hit, 0.0 otherwise.
     */
    fun update(raw: Double, wasCorrect: Double) {
        val p = calibrate(raw)
        val dLogit = p - wasCorrect
        // Gradient of log-loss w.r.t. (a, b): dLogit * (x, 1)
        a -= lr * dLogit * raw
        b -= lr * dLogit
        n++
    }

    fun snapshot(): Pair<Double, Double> = a to b
    fun restore(a: Double, b: Double, n: Int) { this.a = a; this.b = b; this.n = n }
    fun sampleCount(): Int = n
}

/**
 * Binning-based isotonic-style calibrator used as a fallback when Platt
 * calibration has too few samples (n < 50). Maps raw confidence into one of
 * 10 equal-width bins and reports the empirical success rate of each bin.
 */
class BinningCalibrator(private val bins: Int = 10) {
    private val success = IntArray(bins)
    private val total = IntArray(bins)

    fun calibrate(x: Double): Double {
        val bin = (x * bins).toInt().coerceIn(0, bins - 1)
        if (total[bin] == 0) return x
        return success[bin].toDouble() / total[bin]
    }

    fun update(raw: Double, wasCorrect: Double) {
        val bin = (raw * bins).toInt().coerceIn(0, bins - 1)
        total[bin]++
        if (wasCorrect >= 0.5) success[bin]++
    }

    fun sampleCount(): Int = total.sum()
}

/**
 * Composes Platt + binning. Returns Platt when it has enough samples,
 * otherwise falls back to binning, otherwise returns the raw value.
 */
class ConfidenceCalibrator {
    private val platt = PlattCalibrator()
    private val binning = BinningCalibrator()

    fun calibrate(raw: Double): Double = when {
        platt.sampleCount() >= 50 -> platt.calibrate(raw)
        binning.sampleCount() >= 20 -> binning.calibrate(raw)
        else -> raw
    }

    fun update(raw: Double, wasCorrect: Double) {
        platt.update(raw, wasCorrect)
        binning.update(raw, wasCorrect)
    }

    fun snapshot(): String = "${platt.snapshot().first},${platt.snapshot().second},${platt.sampleCount()};${binning.sampleCount()}"
}
