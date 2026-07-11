package com.aicolorpredict.analytics.domain.usecase

import com.aicolorpredict.analytics.ai.base.ModelRegistry
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.metrics.MetricsCalculator
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Recomputes every model's [com.aicolorpredict.analytics.domain.model.ModelPerformance]
 * from the resolved predictions table, then persists the snapshots so the UI
 * can render them without recomputing.
 *
 * Also updates each model's [com.aicolorpredict.analytics.ai.calibration.ConfidenceCalibrator]
 * with the most recent (raw_confidence, was_correct) observations — this is
 * the "self-learning" step.
 *
 * Should be invoked after every [AddRoundUseCase] call (so predictions for the
 * previous round get resolved and fed into the metrics).
 */
class UpdateModelPerformanceUseCase @Inject constructor(
    private val predictionRepo: PredictionRepository,
    private val registry: ModelRegistry,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke() = withContext(dispatchers.default) {
        val now = System.currentTimeMillis()
        val perModel = registry.names.map { name ->
            val recent = predictionRepo.recentResolvedByModel(name, limit = 500)
            if (recent.isEmpty()) return@map null
            val outputs = recent.map { it.first }
            val actuals = recent.map { it.second }
            // Update calibrator with the latest observations (last 50 only).
            recent.takeLast(50).forEach { (out, actual) ->
                val wasCorrect = if (out.topPick == actual) 1.0 else 0.0
                registry.calibratorFor(name).update(out.confidence, wasCorrect)
            }
            MetricsCalculator.perModelPerformance(name, outputs, actuals, rollingWindow = 100, now = now)
        }.filterNotNull()
        predictionRepo.upsertAllPerformance(perModel)
    }
}
