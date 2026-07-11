package com.aicolorpredict.analytics.domain.model

/**
 * The full ensemble prediction for a single round — what the user actually sees.
 *
 * - [topNumbers] is sorted descending by probability and always contains exactly 10 entries.
 *   Their probabilities sum to 1.0.
 * - [modelOutputs] is one entry per participating model (the raw per-model estimates).
 * - [calibratedConfidence] is the post-Platt confidence used for display. It is NOT
 *   the naive max of model confidences; it factors in historical Brier score.
 */
data class Prediction(
    val roundId: Long,
    val epochMs: Long,
    val topNumbers: List<NumberProbability>,
    val colorProbabilities: Map<BallColor, Double>,
    val modelOutputs: List<ModelOutput>,
    val calibratedConfidence: Double,
    val consensusLevel: ConsensusLevel,
    val explanation: String,
    val actualOutcome: Int? = null
) {
    val top1: NumberProbability get() = topNumbers.first()
    val top3: List<NumberProbability> get() = topNumbers.take(3)
    val top5: List<NumberProbability> get() = topNumbers.take(5)
    val top10: List<NumberProbability> get() = topNumbers

    val isCorrect: Boolean? get() = actualOutcome?.let { it == top1.number }
}

data class NumberProbability(
    val number: Int,
    val probability: Double,
    val confidence: Confidence
)

enum class Confidence(val label: String) {
    LOW("Low"), MEDIUM("Medium"), HIGH("High"), VERY_HIGH("Very High");

    companion object {
        fun fromProbability(p: Double, consensus: ConsensusLevel): Confidence {
            // Confidence bands are intentionally conservative — they take consensus
            // into account so a 25% top pick with strong model agreement reads as
            // "Medium" rather than "Low".
            val effective = when (consensus) {
                ConsensusLevel.STRONG -> p + 0.10
                ConsensusLevel.MODERATE -> p + 0.05
                ConsensusLevel.WEAK -> p
            }
            return when {
                effective >= 0.30 -> VERY_HIGH
                effective >= 0.22 -> HIGH
                effective >= 0.16 -> MEDIUM
                else -> LOW
            }
        }
    }
}

enum class ConsensusLevel(val label: String) {
    WEAK("Weak"), MODERATE("Moderate"), STRONG("Strong")
}
