package com.aicolorpredict.analytics.ai.color

import com.aicolorpredict.analytics.domain.model.AppColor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.ln

/**
 * Adaptive reward-based weighting for color models.
 *
 * Each model's score is an EMA of its per-round reward:
 *   reward = 0.5 * top1_hit + 0.3 * log_loss_quality + 0.2 * brier_quality
 *
 * Score decays at 0.95 (half-life ~14 rounds) so recent performance
 * dominates. Weights are softmax(score / temperature).
 */
@Singleton
class ColorAdaptiveWeightingEngine @Inject constructor() {

    data class ModelScore(
        val modelName: String,
        var score: Double = 0.5,
        var samples: Int = 0,
        var hits: Int = 0,
        var recentRewards: ArrayDeque<Double> = ArrayDeque()
    )

    private val scores = mutableMapOf<String, ModelScore>()
    private val decay = 0.95
    private val temperature = 0.12
    private val maxSingleWeight = 0.35

    @Synchronized
    fun registerModel(name: String) {
        scores.getOrPut(name) { ModelScore(name) }
    }

    @Synchronized
    fun recordOutcome(modelName: String, topColor: AppColor, probOfActual: Double, actual: AppColor) {
        val ms = scores.getOrPut(modelName) { ModelScore(modelName) }
        val hit = if (topColor == actual) 1.0 else 0.0
        val logQuality = probOfActual.coerceIn(1e-6, 1.0).let { p -> (1.0 + ln(p) / 3.0).coerceIn(0.0, 1.0) }
        val brierQuality = (2.0 * probOfActual - probOfActual * probOfActual).coerceIn(0.0, 1.0)
        val reward = 0.5 * hit + 0.3 * logQuality + 0.2 * brierQuality
        ms.score = decay * ms.score + (1.0 - decay) * reward
        ms.samples++
        if (hit > 0) ms.hits++
        ms.recentRewards.addLast(reward)
        while (ms.recentRewards.size > 50) ms.recentRewards.removeFirst()
    }

    @Synchronized
    fun computeWeights(modelNames: List<String>, minSamples: Int = 2): Map<String, Double> {
        if (modelNames.isEmpty()) return emptyMap()
        val raw = modelNames.map { name ->
            val ms = scores[name]
            if (ms == null || ms.samples < minSamples) 0.5 else ms.score
        }
        val maxScore = raw.maxOrNull() ?: 0.5
        val exps = raw.map { exp((it - maxScore) / temperature) }
        val sum = exps.sum().coerceAtLeast(1e-9)
        var weights = exps.map { it / sum }.toDoubleArray()
        var iter = 0
        while (weights.maxOrNull()!! > maxSingleWeight && iter < 5) {
            val maxIdx = weights.indices.maxByOrNull { weights[it] }!!
            val overflow = weights[maxIdx] - maxSingleWeight
            weights[maxIdx] = maxSingleWeight
            val others = weights.indices.filter { it != maxIdx }
            val othersSum = others.sumOf { weights[it] }
            if (othersSum > 0) for (i in others) weights[i] += overflow * (weights[i] / othersSum)
            iter++
        }
        return modelNames.zip(weights.toList()).toMap()
    }

    @Synchronized
    fun getRollingAccuracy(modelName: String): Double {
        val ms = scores[modelName] ?: return 0.0
        return if (ms.samples > 0) ms.hits.toDouble() / ms.samples else 0.0
    }

    @Synchronized
    fun rebuildFrom(records: List<Triple<String, AppColor, AppColor>>, probOfActual: (String, AppColor) -> Double) {
        scores.clear()
        for ((name, topColor, actual) in records) {
            val p = probOfActual(name, actual)
            recordOutcome(name, topColor, p, actual)
        }
    }

    @Synchronized
    fun snapshot(): List<ModelScore> = scores.values.toList()
}
