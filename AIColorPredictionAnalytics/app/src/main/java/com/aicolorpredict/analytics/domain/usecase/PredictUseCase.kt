package com.aicolorpredict.analytics.domain.usecase

import com.aicolorpredict.analytics.ai.base.ModelRegistry
import com.aicolorpredict.analytics.ai.calibration.ConfidenceCalibrator
import com.aicolorpredict.analytics.ai.ensemble.AdaptiveWeightingModel
import com.aicolorpredict.analytics.ai.ensemble.EnsembleModel
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
 *   1. Load the most recent N rounds (1000 by default — covers the longest window).
 *   2. Build the [com.aicolorpredict.analytics.domain.model.FeatureSet] for the
 *      *pending* round (the round we want to predict).
 *   3. Ask every model in the [ModelRegistry] for its [com.aicolorpredict.analytics.domain.model.ModelOutput].
 *      Models run in parallel for throughput.
 *   4. Persist every model output via [PredictionRepository].
 *   5. Combine via the [AdaptiveWeightingModel] (which delegates to [EnsembleModel]
 *      with rolling-accuracy boosting).
 *
 * The returned [Prediction] is what every screen displays. It is also persisted
 * (sans the actual outcome) so the history screen can show "what we predicted
 * vs. what happened" once the next round is observed.
 */
class PredictUseCase @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val registry: ModelRegistry,
    private val featureEngineer: FeatureEngineer,
    private val ensemble: EnsembleModel,
    private val adaptive: AdaptiveWeightingModel,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(): Prediction? = withContext(dispatchers.default) {
        val history = roundRepo.lastN(1000).map { it.number }
        if (history.isEmpty()) return@withContext null

        // The "roundId" we predict for is the NEXT round that doesn't exist yet.
        // We use a sentinel — the actual row id is assigned when the next round
        // is added. The Prediction row is keyed to this sentinel so we can
        // resolve it later via [PredictionRepository.resolve].
        val sentinelRoundId = -(System.currentTimeMillis())  // negative to distinguish from real ids

        val features = featureEngineer.build(roundId = sentinelRoundId, history = history)

        // Pull per-model rolling accuracy for the adaptive ensemble.
        val rollingAccuracies = registry.names.associateWith { name ->
            predictionRepo.modelPerformance(name)?.rollingAccuracy ?: 0.1
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

        // Persist outputs (with sentinel roundId).
        predictionRepo.save(sentinelRoundId, outputs)

        // Combine via adaptive ensemble.
        val combined = adaptive.combine(
            roundId = sentinelRoundId,
            epochMs = System.currentTimeMillis(),
            outputs = outputs,
            history = history,
            rollingAccuracies = rollingAccuracies
        )
        combined
    }
}
