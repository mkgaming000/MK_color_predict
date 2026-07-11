package com.aicolorpredict.analytics.ai.markov

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln

/**
 * First-order Markov Chain model.
 *
 * P(next = j) = P(next = j | last = i) where i is the most recent number.
 *
 * Confidence is derived from:
 *   - how concentrated the transition row is (KL divergence from uniform)
 *   - how many observations back that row (more observations = higher confidence)
 *
 * The model falls back to the empirical frequency distribution when the last
 * number has no observed transitions yet.
 */
class MarkovChainModel : PredictionModel {
    override val name: String = "Markov Chain"
    override val category: ModelCategory = ModelCategory.STATISTICAL

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        val last = history.lastOrNull() ?: 0
        val transitionRow = features.transitions[last]
            ?.let { row -> IntArray(10) { row[it] ?: 0 } }
            ?: IntArray(10)

        val totalObs = transitionRow.sumOf { it.toLong() }
        val markovProbs = ModelUtils.laplace(transitionRow, alpha = 1.0)

        // If we have almost no observations for this transition row, blend with
        // the global frequency distribution so we don't make over-confident
        // claims from a handful of samples.
        val globalCounts = IntArray(10) { features.numberFrequency[it] ?: 0 }
        val globalProbs = ModelUtils.laplace(globalCounts, alpha = 2.0)

        val blendWeight = when {
            totalObs < 5 -> 0.3   // mostly global
            totalObs < 20 -> 0.6  // balanced
            else -> 0.9           // mostly Markov
        }
        val blended = ModelUtils.mix(markovProbs, globalProbs, blendWeight)

        val top = blended.indices.maxByOrNull { blended[it] } ?: 0
        val topProb = blended[top]

        // Confidence: combine row concentration with sample size.
        val uniform = 0.1
        val kl = blended.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / uniform) else 0.0) }
        val sampleSizeFactor = 1.0 - exp(-totalObs.toDouble() / 30.0)  // saturates at ~30 obs
        val confidence = ((1.0 - exp(-kl)) * sampleSizeFactor).coerceIn(0.0, 0.85)

        val evidence = buildString {
            append("Last number = $last; ")
            append("${totalObs} observed transitions from $last; ")
            append("top transition $last → $top at ${(topProb * 100).format(1)}%; ")
            append("blended with global freq at weight $blendWeight.")
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
