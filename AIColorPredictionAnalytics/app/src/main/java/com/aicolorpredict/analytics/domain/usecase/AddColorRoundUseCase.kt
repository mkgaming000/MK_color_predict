package com.aicolorpredict.analytics.domain.usecase

import android.util.Log
import com.aicolorpredict.analytics.ai.color.ColorAdaptiveWeightingEngine
import com.aicolorpredict.analytics.ai.color.ColorModelRegistry
import com.aicolorpredict.analytics.data.repository.ColorPredictionRepository
import com.aicolorpredict.analytics.data.repository.ColorRoundRepository
import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Saves a new color round and triggers the learning cycle.
 *
 * Pipeline:
 *   1. Resolve the previous round's predictions.
 *   2. Insert the new round (O(1) — single-row query + single insert).
 *   3. Update adaptive model weights via reward signals.
 *
 * Returns immediately after the insert — the learning cycle runs
 * synchronously but is O(k) where k = number of models.
 */
class AddColorRoundUseCase @Inject constructor(
    private val roundRepo: ColorRoundRepository,
    private val predictionRepo: ColorPredictionRepository,
    private val registry: ColorModelRegistry,
    private val weightingEngine: ColorAdaptiveWeightingEngine,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(color: AppColor, timestamp: Long? = null): Long = withContext(dispatchers.default) {
        val t0 = System.nanoTime()

        // Identify previous round
        val prevRounds = roundRepo.lastN(1)
        val prevRoundId = prevRounds.firstOrNull()?.id

        // Resolve previous predictions
        prevRoundId?.let { prev ->
            predictionRepo.resolve(prev, color.code)
            // Update model weights
            val prevOutputs = predictionRepo.getByRound(prev)
            for (output in prevOutputs) {
                val probOfActual = if (color == AppColor.RED) output.redProbability else output.greenProbability
                weightingEngine.recordOutcome(output.modelName, output.topColor, probOfActual, color)
            }
            Log.d("AddColorRound", "Resolved ${prevOutputs.size} predictions for round $prev")
        }

        // Insert the new round
        val newId = roundRepo.add(color, timestamp)
        val t1 = System.nanoTime()

        // Register all models (idempotent)
        registry.names.forEach { weightingEngine.registerModel(it) }

        Log.d("AddColorRound", "Saved ${color.display} id=$newId in ${(t1 - t0) / 1_000_000}ms")
        newId
    }
}
