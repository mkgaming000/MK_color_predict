package com.aicolorpredict.analytics.domain.model

/**
 * Output of a single AI model for one prediction request.
 *
 * - [numberProbabilities] has exactly 10 entries (one per number 0..9) and MUST sum to 1.0.
 * - [colorProbabilities] maps each [BallColor] to a probability in [0,1]; the union of
 *   Red/Green probabilities MUST be 1.0 (Violet is independent and can be ≤ 0.10 in
 *   practice because only 0 and 5 carry it).
 * - [confidence] is in [0,1] and reflects the model's own self-reported certainty
 *   (calibrated separately before display).
 * - [reason] is a human-readable explanation of *why* the model landed on its top pick.
 *   This is surfaced verbatim in the UI's "AI Explanation" card.
 * - [accuracy] is the model's rolling Top-1 accuracy over the last N predictions (see
 *   [com.aicolorpredict.analytics.metrics.MetricsCalculator]).
 *
 * Implementations of [com.aicolorpredict.analytics.ai.base.PredictionModel] build these
 * objects; the ensemble layer composes them.
 */
data class ModelOutput(
    val modelName: String,
    val numberProbabilities: Map<Int, Double>,
    val colorProbabilities: Map<BallColor, Double>,
    val confidence: Double,
    val reason: String,
    val accuracy: Double,
    val topPick: Int,
    val topProbability: Double
) {
    init {
        require(numberProbabilities.size == 10) {
            "Number probabilities must have 10 entries (0..9), got ${numberProbabilities.size}"
        }
        val sum = numberProbabilities.values.sum()
        require(sum in 0.999..1.001) {
            "Number probabilities must sum to 1.0, got $sum for model $modelName"
        }
        require(confidence in 0.0..1.0) { "Confidence out of range: $confidence" }
        require(accuracy in 0.0..1.0) { "Accuracy out of range: $accuracy" }
    }

    companion object {
        /**
         * Builds a [ModelOutput] from a raw probability vector (length 10, indices 0..9).
         * The vector is normalised defensively and the colour probabilities are derived
         * deterministically from the number probabilities — every model therefore emits
         * consistent colour estimates even if it only "thinks" about numbers.
         */
        fun fromVector(
            modelName: String,
            raw: DoubleArray,
            confidence: Double,
            reason: String,
            accuracy: Double
        ): ModelOutput {
            require(raw.size == 10) { "Vector must have 10 entries, got ${raw.size}" }
            require(raw.all { it.isFinite() && it >= 0.0 }) {
                "All probabilities must be finite and non-negative"
            }
            val sum = raw.sum()
            val normalised = if (sum <= 0.0) DoubleArray(10) { 1.0 / 10.0 } else raw.map { it / sum }.toDoubleArray()
            val numberProbabilities = (0..9).associateWith { normalised[it] }

            var pRed = 0.0
            var pGreen = 0.0
            var pViolet = 0.0
            for (n in 0..9) {
                val p = normalised[n]
                val cols = colorsForNumber(n)
                if (cols.contains(BallColor.RED)) pRed += p
                if (cols.contains(BallColor.GREEN)) pGreen += p
                if (cols.contains(BallColor.VIOLET)) pViolet += p
            }
            // Renormalise Red+Green to sum to 1.0 (Violet is a separate flag probability).
            val rg = pRed + pGreen
            if (rg > 0) { pRed /= rg; pGreen /= rg }

            val topPick = normalised.indices.maxByOrNull { normalised[it] } ?: 0
            val topProbability = normalised[topPick]

            return ModelOutput(
                modelName = modelName,
                numberProbabilities = numberProbabilities,
                colorProbabilities = mapOf(
                    BallColor.RED to pRed,
                    BallColor.GREEN to pGreen,
                    BallColor.VIOLET to pViolet
                ),
                confidence = confidence.coerceIn(0.0, 1.0),
                reason = reason,
                accuracy = accuracy.coerceIn(0.0, 1.0),
                topPick = topPick,
                topProbability = topProbability
            )
        }
    }
}
