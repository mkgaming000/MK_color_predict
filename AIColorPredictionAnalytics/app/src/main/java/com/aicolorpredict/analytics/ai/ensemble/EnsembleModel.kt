package com.aicolorpredict.analytics.ai.ensemble

import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.calibration.ConfidenceCalibrator
import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.Confidence
import com.aicolorpredict.analytics.domain.model.ConsensusLevel
import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.aicolorpredict.analytics.domain.model.NumberProbability
import com.aicolorpredict.analytics.domain.model.Prediction
import com.aicolorpredict.analytics.feature.PatternDetector
import kotlin.math.ln

/**
 * Ensemble Learning model.
 *
 * Combines the per-model [ModelOutput]s into the final [Prediction] the user
 * sees. Weighting scheme:
 *
 *   w_i = softmax( accuracy_i + λ * confidence_i + μ * ln(n_samples_i + 1) )
 *
 * where λ, μ are tunable constants (default 0.5, 0.3). Models with more
 * historical accuracy, higher self-confidence, and more observed samples get
 * more weight — but never so much that a single model dominates (we cap any
 * single weight at 0.35).
 *
 * Consensus level is derived from how many of the top-3 models agree on the
 * same top pick:
 *   - STRONG: >= 70% of models share the same top pick
 *   - MODERATE: 40-70%
 *   - WEAK: < 40%
 *
 * The ensemble never produces a confidence > 0.85. We deliberately understate
 * confidence — the system's job is to be honest, not impressive.
 */
class EnsembleModel(
    private val accuracyWeight: Double = 1.0,
    private val confidenceWeight: Double = 0.5,
    private val sampleWeight: Double = 0.3,
    private val maxSingleWeight: Double = 0.35,
    private val calibratorLookup: (String) -> ConfidenceCalibrator? = { null }
) {
    fun combine(
        roundId: Long,
        epochMs: Long,
        outputs: List<ModelOutput>,
        history: List<Int>
    ): Prediction {
        require(outputs.isNotEmpty()) { "Cannot ensemble an empty list of model outputs" }

        // Compute raw weights.
        val rawWeights = DoubleArray(outputs.size)
        for (i in outputs.indices) {
            val o = outputs[i]
            val logSamples = ln(o.accuracy.coerceAtLeast(0.0) + 0.01) // accuracy already in [0,1]
            val raw = accuracyWeight * o.accuracy +
                      confidenceWeight * o.confidence +
                      sampleWeight * logSamples
            rawWeights[i] = raw
        }
        // Softmax over raw weights.
        val maxW = rawWeights.maxOrNull() ?: 0.0
        val expW = DoubleArray(outputs.size) { exp(rawWeights[it] - maxW) }
        val sumExp = expW.sum()
        var weights = DoubleArray(outputs.size) { expW[it] / sumExp }

        // Cap any single weight at maxSingleWeight, redistribute overflow.
        var iter = 0
        while (weights.maxOrNull()!! > maxSingleWeight && iter < 5) {
            val maxIdx = weights.indices.maxByOrNull { weights[it] }!!
            val overflow = weights[maxIdx] - maxSingleWeight
            weights[maxIdx] = maxSingleWeight
            val others = weights.indices.filter { it != maxIdx }
            val othersSum = others.sumOf { weights[it] }
            if (othersSum > 0) {
                for (i in others) weights[i] += overflow * (weights[i] / othersSum)
            }
            iter++
        }

        // Aggregate number probabilities.
        val aggregated = DoubleArray(10)
        for (i in outputs.indices) {
            val o = outputs[i]
            for (j in 0..9) aggregated[j] += weights[i] * (o.numberProbabilities[j] ?: 0.0)
        }
        val probs = ModelUtils.normalise(aggregated)

        // Aggregate color probabilities.
        var pRed = 0.0; var pGreen = 0.0; var pViolet = 0.0
        for (i in outputs.indices) {
            val o = outputs[i]
            pRed += weights[i] * (o.colorProbabilities[BallColor.RED] ?: 0.0)
            pGreen += weights[i] * (o.colorProbabilities[BallColor.GREEN] ?: 0.0)
            pViolet += weights[i] * (o.colorProbabilities[BallColor.VIOLET] ?: 0.0)
        }
        val rg = pRed + pGreen; if (rg > 0) { pRed /= rg; pGreen /= rg }

        // Top 10 numbers sorted desc.
        val topNumbers = probs.indices
            .map { NumberProbability(it, probs[it], Confidence.LOW) } // confidence set below
            .sortedByDescending { it.probability }
            .map { it.copy(confidence = Confidence.fromProbability(it.probability, consensusLevel(outputs))) }

        // Consensus level from top-pick agreement.
        val consensus = consensusLevel(outputs)

        // Calibrated confidence = weighted average of calibrated per-model confidences.
        val calibratedConf = outputs.indices.fold(0.0) { acc, i ->
            val o = outputs[i]
            val cal = calibratorLookup(o.modelName)?.calibrate(o.confidence) ?: o.confidence
            acc + weights[i] * cal
        }.coerceAtMost(0.85)

        // Build the explanation string.
        val topPick = topNumbers.first().number
        val topProb = topNumbers.first().probability
        val explanation = buildExplanation(outputs, weights, topPick, topProb, consensus, history)

        return Prediction(
            roundId = roundId,
            epochMs = epochMs,
            topNumbers = topNumbers,
            colorProbabilities = mapOf(
                BallColor.RED to pRed, BallColor.GREEN to pGreen, BallColor.VIOLET to pViolet
            ),
            modelOutputs = outputs,
            calibratedConfidence = calibratedConf,
            consensusLevel = consensus,
            explanation = explanation
        )
    }

    private fun consensusLevel(outputs: List<ModelOutput>): ConsensusLevel {
        val topPicks = outputs.map { it.topPick }
        val mostCommon = topPicks.groupBy { it }.maxByOrNull { it.value.size }?.value?.size ?: 0
        val frac = mostCommon.toDouble() / outputs.size
        return when {
            frac >= 0.7 -> ConsensusLevel.STRONG
            frac >= 0.4 -> ConsensusLevel.MODERATE
            else -> ConsensusLevel.WEAK
        }
    }

    private fun buildExplanation(
        outputs: List<ModelOutput>,
        weights: DoubleArray,
        topPick: Int,
        topProb: Double,
        consensus: ConsensusLevel,
        history: List<Int>
    ): String {
        val sb = StringBuilder()
        sb.append("Ensemble favours number $topPick at ${(topProb * 100).format(1)}% (consensus: ${consensus.label}). ")
        sb.append("Model agreement: ")
        val topPicks = outputs.map { it.topPick }
        val grouped = topPicks.groupBy { it }.entries.sortedByDescending { it.value.size }
        sb.append(grouped.joinToString(", ") { "${it.key}→${it.value.size} model(s)" })
        sb.append(". ")
        // Top 3 weighted contributors
        val top3 = outputs.indices.sortedByDescending { weights[it] }.take(3)
        sb.append("Top contributors: ")
        sb.append(top3.joinToString(", ") { i ->
            "${outputs[i].modelName} (${(weights[i] * 100).format(0)}%)"
        })
        sb.append(". ")
        // Pattern summary
        val pattern = PatternDetector.summarise(history)
        sb.append("Pattern detection: $pattern")
        return sb.toString().trim()
    }

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
    private fun exp(x: Double): Double = kotlin.math.exp(x)
}

/**
 * Adaptive Weighting model — a thin wrapper around [EnsembleModel] that uses
 * the most recent rolling accuracy to *dynamically* adjust each model's
 * weight. When a model's rolling accuracy drops, its weight is suppressed
 * more aggressively than the static ensemble would do.
 *
 * Exposed as its own [PredictionModel] so it can be displayed in the Models
 * comparison screen as a separate entry.
 */
class AdaptiveWeightingModel(
    private val base: EnsembleModel,
    private val dynamicBoost: Double = 0.6
) {
    fun combine(
        roundId: Long,
        epochMs: Long,
        outputs: List<ModelOutput>,
        history: List<Int>,
        rollingAccuracies: Map<String, Double>
    ): Prediction {
        // Boost each output's reported accuracy with the rolling figure so the
        // ensemble's softmax weighting leans toward currently-in-form models.
        val boosted = outputs.map { o ->
            val ra = rollingAccuracies[o.modelName] ?: o.accuracy
            o.copy(accuracy = (o.accuracy * (1 - dynamicBoost) + ra * dynamicBoost).coerceIn(0.0, 1.0))
        }
        return base.combine(roundId, epochMs, outputs = boosted, history = history)
    }
}
