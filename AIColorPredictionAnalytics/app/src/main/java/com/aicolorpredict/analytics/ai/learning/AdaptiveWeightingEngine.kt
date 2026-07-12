package com.aicolorpredict.analytics.ai.learning

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Adaptive reward-based model weighting system.
 *
 * Each model maintains a running "reward score" that goes up when its
 * predictions align with observed outcomes and down when they don't.
 * The ensemble weight is derived from this score via softmax, so models
 * that consistently perform well gradually dominate while underperformers
 * are gently demoted — never deleted.
 *
 * **Reward signal** (per resolved round, per model):
 *
 *   reward = α · top1_hit + β · top3_hit + γ · log_loss_quality + δ · brier_quality
 *
 * where:
 *   - top1_hit = 1.0 if the model's top pick matched the actual, else 0.0
 *   - top3_hit = 1.0 if the actual was in the model's top 3, else 0.0
 *   - log_loss_quality = 1.0 - normalised(log_loss) ∈ [0, 1]
 *   - brier_quality = 1.0 - normalised(brier) ∈ [0, 1]
 *
 * The reward is folded into the running score with exponential decay so
 * recent performance matters more than ancient history:
 *
 *   score_t = decay · score_{t-1} + (1 - decay) · reward_t
 *
 * Decay defaults to 0.95, giving a half-life of ~14 rounds. This means a
 * model needs to perform well *recently* to maintain a high weight — it
 * naturally tracks concept drift without explicit reset logic.
 *
 * **Weight derivation:**
 *
 *   weight_i = exp(score_i / τ) / Σ_j exp(score_j / τ)
 *
 * where τ (temperature) controls how sharply the ensemble favours the
 * best model. A cap of 0.35 prevents any single model from monopolising.
 *
 * All state is process-lifetime; on cold start the scores are rebuilt from
 * the persisted prediction history via [rebuildFrom].
 */
@Singleton
class AdaptiveWeightingEngine @Inject constructor() {

    data class ModelScore(
        val modelName: String,
        var score: Double = 0.5,          // composite reward score ∈ [0, 1]
        var historicalScore: Double = 0.5,   // EMA of top-1 hit rate
        var calibrationScore: Double = 0.5,  // inverse Brier (higher = better calibrated)
        var reliabilityScore: Double = 0.5,  // inverse variance of recent rewards
        var confidenceScore: Double = 0.5,   // average top-probability assigned
        var samples: Int = 0,
        var top1Hits: Int = 0,
        var top3Hits: Int = 0,
        var recentRewards: ArrayDeque<Double> = ArrayDeque(),
        var recentTopProbs: ArrayDeque<Double> = ArrayDeque()
    )

    private val scores = mutableMapOf<String, ModelScore>()
    private val recentRewardsMax = 50  // keep last 50 for drift detection

    // Reward weights (sum to 1.0)
    private val alpha = 0.40  // top-1 hit
    private val beta = 0.25   // top-3 hit
    private val gamma = 0.20  // log-loss quality
    private val delta = 0.15  // brier quality

    // Score decay (higher = longer memory)
    private val decay = 0.95

    // Softmax temperature
    private val temperature = 0.15

    // Max single-model weight
    private val maxSingleWeight = 0.35

    /**
     * Initialise or reset the score for a model. Called when a model is first
     * registered (every model starts at 0.5 — neutral).
     */
    @Synchronized
    fun registerModel(name: String) {
        scores.getOrPut(name) { ModelScore(name) }
    }

    /**
     * Record a resolved prediction and update the model's 4 scores.
     *
     * @param modelName the model being scored
     * @param topPick the model's top-1 pick
     * @param top3 the model's top-3 picks
     * @param probOfActual the probability the model assigned to the actual outcome
     * @param topProbability the model's top probability (for confidence tracking)
     * @param actual the observed outcome
     */
    @Synchronized
    fun recordOutcome(
        modelName: String,
        topPick: Int,
        top3: List<Int>,
        probOfActual: Double,
        topProbability: Double = 0.1,
        actual: Int
    ) {
        val ms = scores.getOrPut(modelName) { ModelScore(modelName) }

        val top1Hit = if (topPick == actual) 1.0 else 0.0
        val top3Hit = if (actual in top3) 1.0 else 0.0

        // --- Score 1: Historical (EMA of top-1 hit rate) ---
        ms.historicalScore = decay * ms.historicalScore + (1.0 - decay) * top1Hit

        // --- Score 2: Calibration (inverse Brier) ---
        // Brier for this single observation = (probOfActual - 1)² + Σ(other probs)²
        // Simplified: calibration quality = 1 - (1 - probOfActual)² ∈ [0, 1]
        val brierQuality = (2.0 * probOfActual - probOfActual * probOfActual).coerceIn(0.0, 1.0)
        ms.calibrationScore = decay * ms.calibrationScore + (1.0 - decay) * brierQuality

        // --- Score 3: Reliability (inverse variance of recent rewards) ---
        val logLossQuality = probOfActual.coerceIn(1e-6, 1.0).let { p ->
            (1.0 + ln(p) / 5.0).coerceIn(0.0, 1.0)
        }
        val reward = alpha * top1Hit + beta * top3Hit + gamma * logLossQuality + delta * brierQuality
        ms.recentRewards.addLast(reward)
        while (ms.recentRewards.size > recentRewardsMax) ms.recentRewards.removeFirst()
        // Reliability = 1 - normalised_variance of recent rewards
        val recentMean = ms.recentRewards.average()
        val recentVar = ms.recentRewards.fold(0.0) { acc, r -> acc + (r - recentMean) * (r - recentMean) } / ms.recentRewards.size.coerceAtLeast(1)
        ms.reliabilityScore = (1.0 - recentVar * 4.0).coerceIn(0.0, 1.0)  // scale: var=0.25 → reliability 0

        // --- Score 4: Confidence (EMA of top probability) ---
        ms.recentTopProbs.addLast(topProbability)
        while (ms.recentTopProbs.size > recentRewardsMax) ms.recentTopProbs.removeFirst()
        ms.confidenceScore = ms.recentTopProbs.average().coerceIn(0.0, 1.0)

        // Composite score = weighted average of all 4
        ms.score = 0.35 * ms.historicalScore +
                   0.25 * ms.calibrationScore +
                   0.20 * ms.reliabilityScore +
                   0.20 * ms.confidenceScore

        ms.samples++
        if (top1Hit > 0) ms.top1Hits++
        if (top3Hit > 0) ms.top3Hits++
    }

    /**
     * Compute the current ensemble weights via softmax over scores.
     *
     * Models with fewer than `minSamples` resolved predictions get a
     * uniform prior so new models aren't starved before they've had a
     * chance to prove themselves.
     */
    @Synchronized
    fun computeWeights(modelNames: List<String>, minSamples: Int = 3): Map<String, Double> {
        if (modelNames.isEmpty()) return emptyMap()

        // Each model contributes a score; unregistered models get the neutral 0.5.
        val rawScores = modelNames.map { name ->
            val ms = scores[name]
            if (ms == null || ms.samples < minSamples) {
                0.5  // neutral prior for new/unproven models
            } else {
                ms.score
            }
        }

        // Softmax with temperature
        val maxScore = rawScores.maxOrNull() ?: 0.5
        val expScores = rawScores.map { exp((it - maxScore) / temperature) }
        val sumExp = expScores.sum().coerceAtLeast(1e-9)

        var weights = expScores.map { it / sumExp }.toDoubleArray()

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

        return modelNames.zip(weights.toList()).toMap()
    }

    /** Get the current score for a model (0.5 = neutral). */
    @Synchronized
    fun getScore(modelName: String): Double = scores[modelName]?.score ?: 0.5

    /** Get rolling Top-1 accuracy for a model. */
    @Synchronized
    fun getRollingTop1(modelName: String): Double {
        val ms = scores[modelName] ?: return 0.0
        if (ms.samples == 0) return 0.0
        return ms.top1Hits.toDouble() / ms.samples
    }

    /**
     * Detect concept drift for a model.
     *
     * Compares the mean of the most recent 10 rewards to the mean of the
     * 10 before that. If the recent mean is significantly lower (more than
     * 1.5 standard deviations), the model is drifting.
     */
    @Synchronized
    fun isDrifting(modelName: String): Boolean {
        val ms = scores[modelName] ?: return false
        if (ms.recentRewards.size < 20) return false
        val recent = ms.recentRewards.takeLast(10)
        val older = ms.recentRewards.dropLast(10).takeLast(10)
        val recentMean = recent.average()
        val olderMean = older.average()
        val recentStd = sqrt(recent.map { (it - recentMean).let { d -> d * d } }.average())
        return (olderMean - recentMean) > 1.5 * recentStd
    }

    /**
     * Rebuild scores from a list of historical resolved predictions.
     * Called on cold start.
     */
    @Synchronized
    fun rebuildFrom(records: List<ResolvedRecord>) {
        scores.clear()
        for (r in records) {
            recordOutcome(r.modelName, r.topPick, r.top3, r.probOfActual, r.topProbability, r.actual)
        }
    }

    /** Snapshot of all model scores — for debugging / UI display. */
    @Synchronized
    fun snapshot(): List<ModelScoreSnapshot> = scores.values.map {
        ModelScoreSnapshot(
            modelName = it.modelName,
            score = it.score,
            historicalScore = it.historicalScore,
            calibrationScore = it.calibrationScore,
            reliabilityScore = it.reliabilityScore,
            confidenceScore = it.confidenceScore,
            samples = it.samples,
            top1Accuracy = if (it.samples > 0) it.top1Hits.toDouble() / it.samples else 0.0,
            top3Accuracy = if (it.samples > 0) it.top3Hits.toDouble() / it.samples else 0.0,
            drifting = isDrifting(it.modelName)
        )
    }

    data class ResolvedRecord(
        val modelName: String,
        val topPick: Int,
        val top3: List<Int>,
        val probOfActual: Double,
        val topProbability: Double,
        val actual: Int
    )

    data class ModelScoreSnapshot(
        val modelName: String,
        val score: Double,
        val historicalScore: Double,
        val calibrationScore: Double,
        val reliabilityScore: Double,
        val confidenceScore: Double,
        val samples: Int,
        val top1Accuracy: Double,
        val top3Accuracy: Double,
        val drifting: Boolean
    )
}
