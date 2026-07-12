package com.aicolorpredict.analytics.domain.usecase

import android.util.Log
import com.aicolorpredict.analytics.ai.color.ColorAdaptiveWeightingEngine
import com.aicolorpredict.analytics.ai.color.ColorModelRegistry
import com.aicolorpredict.analytics.ai.color.ColorEnsembleModel
import com.aicolorpredict.analytics.data.repository.ColorPredictionRepository
import com.aicolorpredict.analytics.data.repository.ColorRoundRepository
import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.domain.model.ColorPrediction
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Generates the next color prediction by running all color models in
 * parallel and combining via the adaptive ensemble.
 */
class PredictColorUseCase @Inject constructor(
    private val roundRepo: ColorRoundRepository,
    private val predictionRepo: ColorPredictionRepository,
    private val registry: ColorModelRegistry,
    private val ensemble: ColorEnsembleModel,
    private val weightingEngine: ColorAdaptiveWeightingEngine,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(): ColorPrediction? = withContext(dispatchers.default) {
        val t0 = System.nanoTime()
        val history = roundRepo.lastN(1000).map { it.color }
        if (history.isEmpty()) return@withContext null

        val anchorRoundId = roundRepo.lastN(1).firstOrNull()?.id ?: return@withContext null
        val t1 = System.nanoTime()

        // Get adaptive weights
        val weights = weightingEngine.computeWeights(registry.names)
        val rollingAccuracies = registry.names.associateWith { weightingEngine.getRollingAccuracy(it) }
        val t2 = System.nanoTime()

        // Run all models in parallel
        val outputs = coroutineScope {
            registry.models.map { model ->
                async(dispatchers.default) {
                    val ra = rollingAccuracies[model.name] ?: 0.1
                    model.predict(history, ra)
                }
            }.map { it.await() }
        }
        val t3 = System.nanoTime()

        // Persist
        predictionRepo.save(anchorRoundId, outputs)
        val t4 = System.nanoTime()

        // Combine
        val combined = ensemble.combine(
            roundId = anchorRoundId,
            timestamp = System.currentTimeMillis(),
            outputs = outputs,
            history = history,
            weights = weights
        )
        val t5 = System.nanoTime()

        Log.d("PredictColor", "top=${combined.topColor} (${"%.1f".format(combined.topProbability * 100)}%) | " +
            "load=${(t1 - t0) / 1_000_000}ms weights=${(t2 - t1) / 1_000_000}ms " +
            "models=${(t3 - t2) / 1_000_000}ms save=${(t4 - t3) / 1_000_000}ms " +
            "ensemble=${(t5 - t4) / 1_000_000}ms total=${(t5 - t0) / 1_000_000}ms")

        combined
    }
}
