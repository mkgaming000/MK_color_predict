package com.aicolorpredict.analytics.ai.frequency

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.sqrt

/**
 * Frequency Analysis model.
 *
 * Estimates the next number's distribution as a smoothed version of the
 * empirical frequency in the recent window. Confidence is derived from the
 * KL divergence between the observed distribution and uniform — the further
 * from uniform, the more confident the model is that the next draw is *not*
 * uniform (i.e. some numbers are statistically more likely than others).
 *
 * This is the simplest model in the system and acts as the baseline against
 * which more sophisticated models are judged.
 */
class FrequencyAnalysisModel : PredictionModel {
    override val name: String = "Frequency Analysis"
    override val category: ModelCategory = ModelCategory.STATISTICAL

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        val counts = IntArray(10) { features.numberFrequency[it] ?: 0 }
        val probs = ModelUtils.laplace(counts, alpha = 2.0)
        val top = probs.indices.maxByOrNull { probs[it] } ?: 0
        val topProb = probs[top]

        // Confidence: function of how non-uniform the distribution is.
        val uniform = 1.0 / 10.0
        val kl = probs.fold(0.0) { acc, p -> acc + (if (p > 0) p * kotlin.math.ln(p / uniform) else 0.0) }
        val confidence = (1.0 - kotlin.math.exp(-kl)).coerceIn(0.0, 0.85) // capped at 0.85 — never overstate

        val evidence = buildString {
            append("Observed ${features.totalSamples} rounds; ")
            append("hot numbers: ${features.hotNumbers.joinToString()}; ")
            append("cold numbers: ${features.coldNumbers.joinToString()}; ")
            append("distribution entropy ${(features.entropy).format(3)} nats (uniform ≈ 2.303).")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = probs,
            confidence = confidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
