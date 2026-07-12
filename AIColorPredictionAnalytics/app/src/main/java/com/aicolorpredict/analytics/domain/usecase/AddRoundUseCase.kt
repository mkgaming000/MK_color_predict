package com.aicolorpredict.analytics.domain.usecase

import android.util.Log
import com.aicolorpredict.analytics.ai.learning.IncrementalStatsCache
import com.aicolorpredict.analytics.ai.learning.SelfLearningOrchestrator
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Adds a new round to the database and triggers the full self-learning cycle.
 *
 * Pipeline:
 *   1. Resolve the previous round's predictions against the new outcome.
 *   2. Insert the new round into Room.
 *   3. Update the incremental statistics cache (O(1)).
 *   4. Update adaptive model weights via reward signals.
 *   5. Run concept drift detection.
 *   6. Persist updated performance metrics.
 *
 * The caller (ViewModel) is responsible for generating the next prediction
 * via [PredictUseCase] after this use case completes.
 */
class AddRoundUseCase @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val orchestrator: SelfLearningOrchestrator,
    private val statsCache: IncrementalStatsCache,
    private val dispatchers: AppDispatchers
) {
    /**
     * @param number the actual number that just came up (0..9)
     * @param epochMs optional timestamp; defaults to now
     * @return the new round's row id
     */
    suspend operator fun invoke(number: Int, epochMs: Long? = null): Long = withContext(dispatchers.default) {
        require(number in 0..9) { "Number must be 0..9, was $number" }

        // Ensure the learning engine is initialised (no-op if already done)
        orchestrator.initialise()

        // Identify the previous round (the one whose outcome we now know)
        val previousRounds = roundRepo.lastN(1)
        val previousRoundId = previousRounds.firstOrNull()?.id

        // Resolve the previous round's predictions before inserting the new round
        previousRoundId?.let { prev ->
            predictionRepo.resolve(prev, number)
            Log.d("AddRound", "Resolved predictions for round $prev with actual=$number")
        }

        // Insert the new round
        val newId = roundRepo.add(number, epochMs)
        Log.d("AddRound", "Inserted new round id=$newId number=$number")

        // Trigger the self-learning cycle (O(1) stats + reward update + drift check)
        orchestrator.onNewRound(number, previousRoundId)

        newId
    }
}
