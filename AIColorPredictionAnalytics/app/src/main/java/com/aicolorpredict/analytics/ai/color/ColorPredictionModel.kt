package com.aicolorpredict.analytics.ai.color

import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.domain.model.ColorModelOutput
import kotlin.math.exp
import kotlin.math.ln

/** Base interface + shared utilities for color-only models. */
interface ColorPredictionModel {
    val name: String
    suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput
}

object ColorStats {
    /** Laplace-smoothed P(RED) from a color list. */
    fun redFrequency(history: List<AppColor>): Double {
        val redCount = history.count { it == AppColor.RED }
        return (redCount + 1.0) / (history.size + 2.0)
    }

    /** Shannon entropy in nats. 0 = pure, ln(2) ≈ 0.693 = uniform. */
    fun entropy(redProb: Double): Double {
        val p = redProb.coerceIn(1e-9, 1.0 - 1e-9)
        return -(p * ln(p) + (1.0 - p) * ln(1.0 - p))
    }

    /** Confidence from probability concentration. */
    fun confidenceFromProb(redProb: Double): Double {
        val excess = (redProb - 0.5).let { if (it < 0) -it else it }  // |p - 0.5|
        // excess=0 → 0.5, excess=0.5 → 1.0
        return (0.5 + excess).coerceIn(0.0, 1.0)
    }
}
