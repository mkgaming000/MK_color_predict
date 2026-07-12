package com.aicolorpredict.analytics.ai.learning

import android.util.Log
import com.aicolorpredict.analytics.ai.base.ModelRegistry
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.usecase.UpdateModelPerformanceUseCase
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The self-learning orchestrator.
 *
 * Coordinates the learning cycle after every new round:
 *
 *   1. **Incremental statistics update** — O(1) update to the
 *      [IncrementalStatsCache] (frequency, transitions, colors, gaps).
 *   2. **Reward update** — for every model that made a prediction for the
 *      previous round, record the outcome in [AdaptiveWeightingEngine] so
 *      its ensemble weight adapts.
 *   3. **Concept drift check** — if the distribution has shifted, the
 *      ensemble naturally down-weights stale models via the EMA decay.
 *   4. **Performance metrics persistence** — recomputes Top-K, LogLoss,
 *      Brier, P/R/F1 for every model and saves to Room.
 *
 * **Does NOT generate predictions** — that's [PredictUseCase]'s job. This
 * breaks what would otherwise be a dependency cycle
 * (Orchestrator → PredictUseCase → Orchestrator).
 *
 * All work runs on [AppDispatchers.default] so the UI thread is never
 * blocked, even for 1M+ history rebuilds.
 */
@Singleton
class SelfLearningOrchestrator @Inject constructor(
    private val statsCache: IncrementalStatsCache,
    private val weightingEngine: AdaptiveWeightingEngine,
    private val driftDetector: ConceptDriftDetector,
    private val registry: ModelRegistry,
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val updatePerformanceUseCase: UpdateModelPerformanceUseCase,
    private val dispatchers: AppDispatchers
) {

    /**
     * Initialise the learning engine on cold start.
     *
     * Rebuilds the incremental stats cache and the adaptive weighting scores
     * from the persisted history. This is the only O(n) operation — every
     * subsequent call to [onNewRound] is O(1) + O(k) where k = number of
     * models.
     */
    suspend fun initialise() = withContext(dispatchers.default) {
        if (statsCache.isInitialized()) return@withContext
        Log.d("SelfLearning", "Initialising from history...")
        val history = roundRepo.lastN(1000000).map { it.number }
        statsCache.rebuildFrom(history)

        // Rebuild weighting scores from resolved predictions.
        val records = mutableListOf<AdaptiveWeightingEngine.ResolvedRecord>()
        for (name in registry.names) {
            val resolved = predictionRepo.recentResolvedByModel(name, limit = 500)
            for ((output, actual) in resolved) {
                if (actual < 0) continue
                val top3 = output.numberProbabilities.entries
                    .sortedByDescending { it.value }
                    .take(3)
                    .map { it.key }
                val probOfActual = output.numberProbabilities[actual] ?: 0.0
                records += AdaptiveWeightingEngine.ResolvedRecord(
                    modelName = name,
                    topPick = output.topPick,
                    top3 = top3,
                    probOfActual = probOfActual,
                    topProbability = output.topProbability,
                    actual = actual
                )
            }
            weightingEngine.registerModel(name)
        }
        weightingEngine.rebuildFrom(records)
        Log.d("SelfLearning", "Initialised: ${history.size} rounds, ${records.size} resolved predictions")
    }

    /**
     * Called after a new round is saved.
     *
     * Performs the self-learning cycle:
     *   1. O(1) stats update
     *   2. Resolve previous round's predictions → reward update
     *   3. Drift check
     *   4. Performance metrics persistence
     *
     * Does NOT generate a new prediction — the caller (ViewModel) is
     * responsible for that via [PredictUseCase] after this returns.
     */
    suspend fun onNewRound(number: Int, previousRoundId: Long?) = withContext(dispatchers.default) {
        // 1. O(1) incremental stats update
        statsCache.update(number)
        Log.d("SelfLearning", "Stats updated for round number=$number")

        // 2. Resolve previous round's predictions and update rewards
        if (previousRoundId != null) {
            val previousOutputs = predictionRepo.getByRound(previousRoundId)
            if (previousOutputs.isNotEmpty()) {
                for (output in previousOutputs) {
                    val top3 = output.numberProbabilities.entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .map { it.key }
                    val probOfActual = output.numberProbabilities[number] ?: 0.0
                    weightingEngine.recordOutcome(
                        modelName = output.modelName,
                        topPick = output.topPick,
                        top3 = top3,
                        probOfActual = probOfActual,
                        topProbability = output.topProbability,
                        actual = number
                    )
                }
                Log.d("SelfLearning", "Rewards updated for ${previousOutputs.size} model predictions")
            }
        }

        // 3. Drift check
        val drift = driftDetector.detect()
        if (drift.drifting) {
            Log.w("SelfLearning", "Concept drift detected: ${drift.description}")
        }

        // 4. Performance metrics persistence
        updatePerformanceUseCase()
    }

    /** Current drift report — for the UI. */
    suspend fun driftReport(): ConceptDriftDetector.DriftReport = withContext(dispatchers.default) {
        driftDetector.detect()
    }

    /** Current model score snapshot — for the UI. */
    suspend fun modelScores(): List<AdaptiveWeightingEngine.ModelScoreSnapshot> =
        withContext(dispatchers.default) {
            weightingEngine.snapshot()
        }
}
