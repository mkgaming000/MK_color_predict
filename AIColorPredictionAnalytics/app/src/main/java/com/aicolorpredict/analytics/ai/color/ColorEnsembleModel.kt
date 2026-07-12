package com.aicolorpredict.analytics.ai.color

import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.domain.model.ColorModelOutput
import com.aicolorpredict.analytics.domain.model.ColorPrediction
import com.aicolorpredict.analytics.domain.model.ConsensusLevel
import kotlin.math.exp
import kotlin.math.ln

/**
 * Color ensemble — combines all color model outputs into a single prediction.
 *
 * Weighting: softmax over each model's accuracy + confidence, with a
 * 0.35 max-single-weight cap so no model dominates. Confidence is
 * capped at 0.85 — the system never claims certainty about a random game.
 */
class ColorEnsembleModel(
    private val maxSingleWeight: Double = 0.35
) {
    fun combine(
        roundId: Long,
        timestamp: Long,
        outputs: List<ColorModelOutput>,
        history: List<AppColor>,
        weights: Map<String, Double>
    ): ColorPrediction {
        require(outputs.isNotEmpty())

        // Compute ensemble weights from the adaptive engine's weights map.
        val modelWeights = DoubleArray(outputs.size)
        for (i in outputs.indices) {
            modelWeights[i] = weights[outputs[i].modelName] ?: (1.0 / outputs.size)
        }
        // Normalise
        val wSum = modelWeights.sum().coerceAtLeast(1e-9)
        for (i in modelWeights.indices) modelWeights[i] /= wSum

        // Cap individual weights
        var iter = 0
        while (modelWeights.maxOrNull()!! > maxSingleWeight && iter < 5) {
            val maxIdx = modelWeights.indices.maxByOrNull { modelWeights[it] }!!
            val overflow = modelWeights[maxIdx] - maxSingleWeight
            modelWeights[maxIdx] = maxSingleWeight
            val others = modelWeights.indices.filter { it != maxIdx }
            val othersSum = others.sumOf { modelWeights[it] }
            if (othersSum > 0) for (i in others) modelWeights[i] += overflow * (modelWeights[i] / othersSum)
            iter++
        }

        // Aggregate
        var redProb = 0.0
        for (i in outputs.indices) redProb += modelWeights[i] * outputs[i].redProbability
        redProb = redProb.coerceIn(0.001, 0.999)
        val greenProb = 1.0 - redProb

        // Consensus
        val topPicks = outputs.map { it.topColor }
        val mostCommon = topPicks.groupBy { it }.maxByOrNull { it.value.size }?.value?.size ?: 0
        val frac = mostCommon.toDouble() / outputs.size
        val consensus = when {
            frac >= 0.7 -> ConsensusLevel.STRONG
            frac >= 0.5 -> ConsensusLevel.MODERATE
            else -> ConsensusLevel.WEAK
        }

        // Confidence
        val calibratedConf = outputs.indices.fold(0.0) { acc, i ->
            acc + modelWeights[i] * outputs[i].confidence
        }.coerceAtMost(0.85)

        // Explanation
        val topColor = if (redProb >= greenProb) AppColor.RED else AppColor.GREEN
        val topProb = maxOf(redProb, greenProb)
        val explanation = buildExplanation(outputs, modelWeights, topColor, topProb, consensus, history)

        return ColorPrediction(
            roundId = roundId,
            timestamp = timestamp,
            redProbability = redProb,
            greenProbability = greenProb,
            confidence = calibratedConf,
            consensusLevel = consensus,
            explanation = explanation,
            modelOutputs = outputs
        )
    }

    private fun buildExplanation(
        outputs: List<ColorModelOutput>,
        weights: DoubleArray,
        topColor: AppColor,
        topProb: Double,
        consensus: ConsensusLevel,
        history: List<AppColor>
    ): String {
        val sb = StringBuilder()
        sb.append("Ensemble favours ${topColor.display} at ${"%.1f".format(topProb * 100)}% ")
        sb.append("(consensus: ${consensus.label}). ")
        sb.append("Based on ${history.size} historical rounds. ")
        val agreeCount = outputs.count { it.topColor == topColor }
        sb.append("${agreeCount}/${outputs.size} models agree. ")
        val top3 = outputs.indices.sortedByDescending { weights[it] }.take(3)
        sb.append("Top contributors: ")
        sb.append(top3.joinToString(", ") { i ->
            "${outputs[i].modelName} (${"%.0f".format(weights[i] * 100)}%)"
        })
        sb.append(". ")
        sb.append("This is a statistical estimate from historical data — not a guarantee.")
        return sb.toString().trim()
    }
}
