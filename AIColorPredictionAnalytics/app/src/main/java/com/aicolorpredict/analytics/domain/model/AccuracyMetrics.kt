package com.aicolorpredict.analytics.domain.model

/**
 * Honest summary of overall system performance across all rounds seen so far.
 *
 * Displayed on the Analytics screen and on the Models comparison screen.
 * Every field is computed from real prediction-vs-actual pairs.
 */
data class AccuracyMetrics(
    val totalPredictions: Int,
    val top1Accuracy: Double,
    val top3Accuracy: Double,
    val top5Accuracy: Double,
    val logLoss: Double,
    val brierScore: Double,
    val macroPrecision: Double,
    val macroRecall: Double,
    val macroF1: Double,
    val rollingAccuracy: Double,
    val rollingWindowSize: Int,
    val confusionMatrix: Map<Int, Map<Int, Int>>
) {
    companion object {
        val EMPTY = AccuracyMetrics(
            totalPredictions = 0,
            top1Accuracy = 0.0,
            top3Accuracy = 0.0,
            top5Accuracy = 0.0,
            logLoss = Double.NaN,
            brierScore = Double.NaN,
            macroPrecision = 0.0,
            macroRecall = 0.0,
            macroF1 = 0.0,
            rollingAccuracy = 0.0,
            rollingWindowSize = 100,
            confusionMatrix = emptyMap()
        )
    }
}
