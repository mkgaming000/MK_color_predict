package com.aicolorpredict.analytics.domain.usecase

import com.aicolorpredict.analytics.ai.base.ModelRegistry
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
 *   2. Identify the **anchor round** — the most recent round in the DB. We predict
 *      the *next* outcome (the round that hasn't happened yet) and key the
 *      persisted predictions to this anchor's id. When the next round is added
 *      via [AddRoundUseCase], it resolves those predictions using the same id.
 *   3. Build the [com.aicolorpredict.analytics.domain.model.FeatureSet] for the
 *      pending round.
 *   4. Ask every model in the [ModelRegistry] for its [com.aicolorpredict.analytics.domain.model.ModelOutput].
 *      Models run in parallel for throughput.
 *   5. Persist every model output via [PredictionRepository] keyed to the anchor.
 *   6. Combine via the [AdaptiveWeightingModel] (which delegates to [EnsembleModel]
 *      with rolling-accuracy boosting).
 *
 * The returned [Prediction] is what every screen displays. It is also persisted
 * (sans the actual outcome) so the history screen can show "what we predicted
 * vs. what happened" once the next round is observed.
 *
 * If there are no rounds yet, returns null (the UI shows an empty state).
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
        val recentRounds = roundRepo.lastN(1000)
        if (recentRounds.isEmpty()) return@withContext null

        val history = recentRounds.map { it.number }
        // Key predictions to the most recent round's id. When the NEXT round is
        // added, AddRoundUseCase will resolve predictions for THIS id.
        val anchorRoundId = recentRounds.first().id

        val features = featureEngineer.build(roundId = anchorRoundId, history = history)

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

        // Persist outputs keyed to the anchor round.
        predictionRepo.save(anchorRoundId, outputs)

        // Combine via adaptive ensemble.
        adaptive.combine(
            roundId = anchorRoundId,
            epochMs = System.currentTimeMillis(),
            outputs = outputs,
            history = history,
            rollingAccuracies = rollingAccuracies
        )
    }
}
