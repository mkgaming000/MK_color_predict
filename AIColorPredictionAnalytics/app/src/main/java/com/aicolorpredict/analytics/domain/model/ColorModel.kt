package com.aicolorpredict.analytics.domain.model

/**
 * The only two colors in the system.
 *
 * The app is deliberately binary — every round is either RED or GREEN.
 * No numbers, no violet, no multi-color logic.
 */
enum class AppColor(val display: String, val code: Int) {
    RED("Red", 0),
    GREEN("Green", 1);

    companion object {
        fun fromCode(code: Int): AppColor = if (code == 1) GREEN else RED
        fun opposite(c: AppColor): AppColor = if (c == RED) GREEN else RED
    }
}

/**
 * A single completed color round.
 *
 * @param id database row id (0 for new rows)
 * @param timestamp epoch milliseconds
 * @param color the observed color
 * @param previousColor the color of the immediately preceding round (null if first)
 * @param streak consecutive same-color count ending at this round (inclusive, >= 1)
 * @param sequenceIndex 0-based position in the full history
 */
data class ColorRound(
    val id: Long,
    val timestamp: Long,
    val color: AppColor,
    val previousColor: AppColor?,
    val streak: Int,
    val sequenceIndex: Int
)

/**
 * Output of a single color model for one prediction request.
 */
data class ColorModelOutput(
    val modelName: String,
    val redProbability: Double,
    val greenProbability: Double,
    val confidence: Double,
    val reason: String,
    val accuracy: Double
) {
    init {
        require(redProbability in 0.0..1.0) { "redProbability out of range: $redProbability" }
        require(greenProbability in 0.0..1.0) { "greenProbability out of range: $greenProbability" }
        val sum = redProbability + greenProbability
        require(sum in 0.999..1.001) { "Probabilities must sum to 1.0, got $sum" }
    }

    val topColor: AppColor get() = if (redProbability >= greenProbability) AppColor.RED else AppColor.GREEN
    val topProbability: Double get() = maxOf(redProbability, greenProbability)

    companion object {
        fun fromRedProb(
            modelName: String,
            redProb: Double,
            confidence: Double,
            reason: String,
            accuracy: Double
        ): ColorModelOutput {
            val rp = redProb.coerceIn(0.0, 1.0)
            val gp = 1.0 - rp
            return ColorModelOutput(
                modelName = modelName,
                redProbability = rp,
                greenProbability = gp,
                confidence = confidence.coerceIn(0.0, 1.0),
                reason = reason,
                accuracy = accuracy.coerceIn(0.0, 1.0)
            )
        }
    }
}

/**
 * The full ensemble color prediction — what the user sees.
 */
data class ColorPrediction(
    val roundId: Long,
    val timestamp: Long,
    val redProbability: Double,
    val greenProbability: Double,
    val confidence: Double,
    val consensusLevel: ConsensusLevel,
    val explanation: String,
    val modelOutputs: List<ColorModelOutput>,
    val actualColor: AppColor? = null
) {
    val topColor: AppColor get() = if (redProbability >= greenProbability) AppColor.RED else AppColor.GREEN
    val topProbability: Double get() = maxOf(redProbability, greenProbability)
    val isCorrect: Boolean? get() = actualColor?.let { it == topColor }
}

enum class ConsensusLevel(val label: String) {
    WEAK("Weak"), MODERATE("Moderate"), STRONG("Strong")
}

/**
 * Per-model rolling performance for the color system.
 */
data class ColorModelPerformance(
    val modelName: String,
    val samplesObserved: Int,
    val top1Accuracy: Double,
    val logLoss: Double,
    val brierScore: Double,
    val rollingAccuracy: Double,
    val lastUpdated: Long
) {
    companion object {
        val EMPTY = ColorModelPerformance(
            modelName = "", samplesObserved = 0,
            top1Accuracy = 0.0, logLoss = Double.NaN, brierScore = Double.NaN,
            rollingAccuracy = 0.0, lastUpdated = 0L
        )
    }
}
