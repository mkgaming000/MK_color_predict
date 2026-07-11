package com.aicolorpredict.analytics.ai.transition

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln

/**
 * Transition-Matrix model — focuses purely on the 10x10 first-order transition
 * counts, but blends in second-order information (last two numbers) when
 * sufficient observations exist.
 *
 * Differs from [com.aicolorpredict.analytics.ai.markov.MarkovChainModel] in that:
 *   - It explicitly tries 2nd-order transitions when sample size allows.
 *   - Its confidence estimate is calibrated to penalise low-sample-size rows
 *     more aggressively.
 */
class TransitionMatrixModel : PredictionModel {
    override val name: String = "Transition Matrix"
    override val category: ModelCategory = ModelCategory.STATISTICAL

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        val last = history.lastOrNull() ?: 0
        val secondLast = if (history.size >= 2) history[history.size - 2] else null

        // First-order row
        val firstOrderCounts = IntArray(10) { features.transitions[last]?.get(it) ?: 0 }
        val firstOrderProbs = ModelUtils.laplace(firstOrderCounts, alpha = 1.0)
        val firstOrderN = firstOrderCounts.sumOf { it.toLong() }

        // Second-order row (only if we have enough samples)
        val secondOrderProbs: DoubleArray? = if (secondLast != null && history.size >= 50) {
            val pairKey = secondLast * 10 + last
            val counts = IntArray(10)
            for (i in 2 until history.size) {
                val key = history[i - 2] * 10 + history[i - 1]
                if (key == pairKey) counts[history[i]]++
            }
            val total = counts.sumOf { it.toLong() }
            if (total >= 10) ModelUtils.laplace(counts, alpha = 1.0) else null
        } else null

        val blended = if (secondOrderProbs != null) {
            // Weight second-order more heavily when sample size is large
            val w = 0.65
            ModelUtils.mix(secondOrderProbs, firstOrderProbs, w)
        } else firstOrderProbs

        val top = blended.indices.maxByOrNull { blended[it] } ?: 0
        val topProb = blended[top]

        val kl = blended.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val sampleFactor = 1.0 - exp(-firstOrderN.toDouble() / 50.0)
        val confidence = ((1.0 - exp(-kl)) * sampleFactor).coerceIn(0.0, 0.85)

        val evidence = buildString {
            append("Last number = $last")
            if (secondLast != null) append(", second-last = $secondLast")
            append("; ")
            append("${firstOrderN} 1st-order transitions; ")
            if (secondOrderProbs != null) append("2nd-order signal available, blended at 0.65; ")
            else append("no 2nd-order signal; ")
            append("top transition target $top at ${(topProb * 100).format(1)}%.")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = blended,
            confidence = confidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
