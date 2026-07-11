package com.aicolorpredict.analytics.domain.model

/**
 * Per-model rolling performance metrics.
 *
 * Every metric is computed from real prediction-vs-actual pairs and is updated
 * incrementally after each completed round. We deliberately cap [rollingWindow]
 * so stale performance decays — models that drift get demoted by the ensemble.
 */
data class ModelPerformance(
    val modelName: String,
    val samplesObserved: Int,
    val top1Accuracy: Double,
    val top3Accuracy: Double,
    val top5Accuracy: Double,
    val logLoss: Double,
    val brierScore: Double,
    val precision: Double,
    val recall: Double,
    val f1: Double,
    val confusionMatrix: Map<Int, Map<Int, Int>>,
    val rollingAccuracy: Double,
    val rollingWindow: Int,
    val lastUpdated: Long
) {
    companion object {
        val EMPTY = ModelPerformance(
            modelName = "",
            samplesObserved = 0,
            top1Accuracy = 0.0,
            top3Accuracy = 0.0,
            top5Accuracy = 0.0,
            logLoss = Double.NaN,
            brierScore = Double.NaN,
            precision = 0.0,
            recall = 0.0,
            f1 = 0.0,
            confusionMatrix = emptyMap(),
            rollingAccuracy = 0.0,
            rollingWindow = 100,
            lastUpdated = 0L
        )
    }
}
