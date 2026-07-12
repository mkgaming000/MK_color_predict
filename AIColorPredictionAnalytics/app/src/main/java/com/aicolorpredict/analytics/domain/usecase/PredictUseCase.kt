package com.aicolorpredict.analytics.domain.usecase

import android.util.Log
import com.aicolorpredict.analytics.ai.base.ModelRegistry
import com.aicolorpredict.analytics.ai.ensemble.AdaptiveWeightingModel
import com.aicolorpredict.analytics.ai.ensemble.EnsembleModel
import com.aicolorpredict.analytics.ai.learning.AdaptiveWeightingEngine
import com.aicolorpredict.analytics.ai.learning.IncrementalStatsCache
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.Prediction
import com.aicolorpredict.analytics.feature.FeatureEngineer
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Core prediction use case.
 *
 * Pipeline:
 *   1. Ensure the learning engine is initialised (one-time O(n) rebuild).
 *   2. Load the most recent 1000 rounds for feature engineering.
 *   3. Identify the anchor round (most recent) — predictions are keyed to
 *      its id so they can be resolved when the next round arrives.
 *   4. Build the [FeatureSet] for the pending round.
 *   5. Ask every model in the [ModelRegistry] for its [ModelOutput] — models
 *      run in parallel on [AppDispatchers.default].
 *   6. Persist every model output (keyed to the anchor round).
 *   7. Combine via the [AdaptiveWeightingModel], which pulls the current
 *      reward-based weights from [AdaptiveWeightingEngine].
 *
 * The returned [Prediction] is what every screen displays.
 */
class PredictUseCase @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val registry: ModelRegistry,
    private val featureEngineer: FeatureEngineer,
    private val ensemble: EnsembleModel,
    private val adaptive: AdaptiveWeightingModel,
    private val weightingEngine: AdaptiveWeightingEngine,
    private val statsCache: IncrementalStatsCache,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(): Prediction? = withContext(dispatchers.default) {
        // Ensure the incremental stats cache is initialised (no-op if already done)
        if (!statsCache.isInitialized()) {
            val history = roundRepo.lastN(1000000).map { it.number }
            statsCache.rebuildFrom(history)
        }

        val recentRounds = roundRepo.lastN(1000)
        if (recentRounds.isEmpty()) return@withContext null

        val history = recentRounds.map { it.number }
        val anchorRoundId = recentRounds.first().id

        Log.d("Predict", "Building prediction for anchor round $anchorRoundId (${history.size} history)")

        val features = featureEngineer.build(roundId = anchorRoundId, history = history)

        // Pull current adaptive weights for the ensemble.
        val weights = weightingEngine.computeWeights(registry.names)
        val rollingAccuracies = registry.names.associateWith { name ->
            weightingEngine.getRollingTop1(name)
        }

        // Run every model in parallel.
        val outputs = coroutineScope {
            registry.models.map { model ->
                async(dispatchers.default) {
                    val ra = rollingAccuracies[model.name] ?: 0.1
                    model.predict(features, history, ra)
                }
            }.map { it.await() }
        }

        // Persist outputs keyed to the anchor round.
        predictionRepo.save(anchorRoundId, outputs)

        // Combine via adaptive ensemble.
        val combined = adaptive.combine(
            roundId = anchorRoundId,
            epochMs = System.currentTimeMillis(),
            outputs = outputs,
            history = history,
            rollingAccuracies = rollingAccuracies
        )

        Log.d("Predict", "Prediction complete: top=${combined.top1.number} (${"%.1f".format(combined.top1.probability * 100)}%)")
        combined
    }
}
