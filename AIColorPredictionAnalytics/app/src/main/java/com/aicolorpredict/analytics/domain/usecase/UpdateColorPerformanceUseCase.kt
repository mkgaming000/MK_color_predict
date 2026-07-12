package com.aicolorpredict.analytics.domain.usecase

import com.aicolorpredict.analytics.data.repository.ColorPredictionRepository
import com.aicolorpredict.analytics.domain.model.ColorModelPerformance
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.ln

/**
 * Recomputes per-model performance metrics from resolved predictions.
 */
class UpdateColorPerformanceUseCase @Inject constructor(
    private val predictionRepo: ColorPredictionRepository,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(modelNames: List<String>) = withContext(dispatchers.default) {
        val now = System.currentTimeMillis()
        val perModel = modelNames.map { name ->
            val resolved = predictionRepo.recentResolvedByModel(name, limit = 500)
            if (resolved.isEmpty()) return@map null

            var hits = 0
            var logLossSum = 0.0
            var brierSum = 0.0
            for ((output, actualCode) in resolved) {
                if (actualCode < 0) continue
                val actualColor = com.aicolorpredict.analytics.domain.model.AppColor.fromCode(actualCode)
                if (output.topColor == actualColor) hits++
                val p = if (actualColor == com.aicolorpredict.analytics.domain.model.AppColor.RED) output.redProbability else output.greenProbability
                logLossSum += -ln(p.coerceAtLeast(1e-12))
                val otherP = 1.0 - p
                brierSum += (p - 1.0) * (p - 1.0) + otherP * otherP
            }
            val n = resolved.size
            ColorModelPerformance(
                modelName = name,
                samplesObserved = n,
                top1Accuracy = hits.toDouble() / n,
                logLoss = logLossSum / n,
                brierScore = brierSum / n,
                rollingAccuracy = hits.toDouble() / n,
                lastUpdated = now
            )
        }.filterNotNull()
        predictionRepo.upsertAllPerformance(perModel)
    }
}
