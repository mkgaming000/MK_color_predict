package com.aicolorpredict.analytics.ai.movingavg

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.aicolorpredict.analytics.feature.StatsCalculator
import kotlin.math.exp

/**
 * Moving Average model.
 *
 * Computes weighted frequency distributions over multiple trailing windows
 * (5, 20, 100) and blends them with linearly-decaying weights favouring the
 * shortest window. The intuition: "what's hot right now is probably more
 * predictive than what was hot 100 rounds ago, but the long window guards
 * against chasing noise."
 *
 * Confidence is calibrated from the agreement between the three windows — high
 * agreement = high confidence, diverging windows = low confidence.
 */
class MovingAverageModel : PredictionModel {
    override val name: String = "Moving Average"
    override val category: ModelCategory = ModelCategory.STATISTICAL

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        val w5 = windowDist(history, 5)
        val w20 = windowDist(history, 20)
        val w100 = windowDist(history, 100)

        // Weighted blend — shortest window weighted highest.
        val blend = DoubleArray(10)
        for (j in 0..9) blend[j] = 0.55 * w5[j] + 0.30 * w20[j] + 0.15 * w100[j]
        val norm = ModelUtils.normalise(blend)

        val top = norm.indices.maxByOrNull { norm[it] } ?: 0
        val topProb = norm[top]

        // Agreement: cosine similarity between w5 and w100.
        val agreement = StatsCalculator.cosine(w5, w100)
        val confidence = (0.4 + 0.45 * agreement).coerceIn(0.0, 0.80)

        val evidence = buildString {
            append("Short window (5) top: ${w5.indices.maxByOrNull { w5[it] } ?: 0}; ")
            append("mid window (20) top: ${w20.indices.maxByOrNull { w20[it] } ?: 0}; ")
            append("long window (100) top: ${w100.indices.maxByOrNull { w100[it] } ?: 0}; ")
            append("agreement score ${agreement.format(3)}; ")
            append("blended top = $top at ${(topProb * 100).format(1)}%.")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = norm,
            confidence = confidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    private fun windowDist(history: List<Int>, w: Int): DoubleArray {
        val tail = history.takeLast(minOf(history.size, w))
        val counts = IntArray(10)
        for (v in tail) counts[v]++
        return ModelUtils.laplace(counts, alpha = 1.0)
    }

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
