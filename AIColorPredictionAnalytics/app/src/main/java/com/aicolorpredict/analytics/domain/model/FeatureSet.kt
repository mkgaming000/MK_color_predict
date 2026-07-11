package com.aicolorpredict.analytics.domain.model

/**
 * Statistical features computed from the history window ending *just before*
 * the round we want to predict. Every AI model consumes this object — it is
 * the single shared "feature payload" of the system.
 *
 * Fields are intentionally grouped so a model can pick what it needs:
 * - [transitions] / [transitionCounts] feed Markov / HMM / Transition models.
 * - [numberFrequency] / [colorFrequency] feed Frequency / Bayesian.
 * - [gaps] / [hotCold] feed Momentum / Gap / Monte Carlo.
 * - [entropy] / [variance] feed calibration & ensemble weighting.
 * - [patterns] feed Pattern Detector-driven models.
 *
 * All probabilities in this object are raw observed ratios — they are NOT
 * smoothed. Smoothing happens inside each model so different models can apply
 * different priors.
 */
data class FeatureSet(
    val roundId: Long,
    val totalSamples: Int,
    val recent: List<Int>,
    val numberFrequency: Map<Int, Int>,
    val colorFrequency: Map<BallColor, Int>,
    val transitions: Map<Int, Map<Int, Int>>,
    val transitionCounts: Map<Int, Int>,
    val gaps: Map<Int, Int>,
    val hotNumbers: List<Int>,
    val coldNumbers: List<Int>,
    val recentMomentum: Map<Int, Double>,
    val longTermMomentum: Map<Int, Double>,
    val oddRatio: Double,
    val evenRatio: Double,
    val smallRatio: Double,
    val bigRatio: Double,
    val greenRatio: Double,
    val redRatio: Double,
    val violetRatio: Double,
    val rollingAverages: Map<Int, Double>,
    val entropy: Double,
    val variance: Double,
    val patternFrequency: Map<String, Int>,
    val cycles: List<Cycle>,
    val runs: List<Run>,
    val timeIntervals: List<Long>,
    val historicalSimilarity: Map<Int, Double>,
    val featureVector: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeatureSet) return false
        return roundId == other.roundId && totalSamples == other.totalSamples
    }
    override fun hashCode(): Int = roundId.hashCode()
}

data class Cycle(val length: Int, val pattern: List<Int>, val occurrences: Int)

data class Run(val color: BallColor, val length: Int, val endedAtRoundId: Long)
