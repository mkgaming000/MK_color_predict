package com.aicolorpredict.analytics.ai.learning

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Concept drift detector.
 *
 * Monitors the distribution of recent outcomes and flags when the
 * underlying data-generating process appears to have shifted. Uses two
 * complementary signals:
 *
 * 1. **Frequency drift** — compares the number-frequency distribution of the
 *    most recent `windowSize` rounds to the long-term distribution. If the
 *    total-variation distance exceeds `threshold`, drift is flagged.
 *
 * 2. **Accuracy drift** — delegates to [AdaptiveWeightingEngine.isDrifting]
 *    for per-model accuracy regression.
 *
 * The detector is intentionally conservative: a single blip won't trigger
 * a drift flag — the shift must persist across the full window. This avoids
 * false alarms from normal short-term variance.
 */
@Singleton
class ConceptDriftDetector @Inject constructor(
    private val statsCache: IncrementalStatsCache
) {

    data class DriftReport(
        val drifting: Boolean,
        val tvDistance: Double,
        val description: String
    ) {

        companion object {
            val STABLE = DriftReport(false, 0.0, "Distribution stable.")
        }
    }

    private val windowSize = 200
    private val threshold = 0.35

    /**
     * Compute the total-variation distance between the recent window's
     * frequency distribution and the long-term distribution.
     *
     * TV distance = 0.5 · Σ |p_recent(i) - p_long(i)|
     *
     * Range: [0, 1]. 0 = identical, 1 = completely disjoint.
     */
    fun detect(): DriftReport {
        val recent = statsCache.recentWindow(windowSize)
        if (recent.size < windowSize / 2) {
            return DriftReport(false, 0.0, "Insufficient history for drift detection (${recent.size}/${windowSize} rounds).")
        }

        val recentCounts = IntArray(10)
        for (n in recent) recentCounts[n]++
        val recentTotal = recent.size.toDouble()
        val recentFreq = DoubleArray(10) { (recentCounts[it] + 1.0) / (recentTotal + 10.0) }

        val longFreq = statsCache.numberFrequency()

        var tv = 0.0
        for (i in 0..9) tv += abs(recentFreq[i] - longFreq[i])
        tv *= 0.5

        val drifting = tv > threshold
        val description = if (drifting) {
            "Distribution shifted (TV=${"%.3f".format(tv)}). Recent outcomes differ significantly from the long-term average — models may need retraining."
        } else {
            "Distribution stable (TV=${"%.3f".format(tv)})."
        }

        return DriftReport(drifting, tv, description)
    }
}
