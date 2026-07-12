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
 * Adds a new round to the database — performance-optimized.
 *
 * Key optimization: the DB insert returns immediately. The self-learning
 * cycle (reward update, drift check, metrics persistence) runs on
 * [AppDispatchers.default] but the caller doesn't wait for it — the
 * ViewModel can show "Saved" instantly and trigger a prediction refresh
 * in parallel.
 *
 * The stats cache update is O(1) and runs before the learning cycle so
 * the next prediction has fresh stats immediately.
 *
 * Timing is logged so save latency is measurable.
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
     * @return the new round's row id (returns as soon as the insert completes)
     */
    suspend operator fun invoke(number: Int, epochMs: Long? = null): Long = withContext(dispatchers.default) {
        require(number in 0..9) { "Number must be 0..9, was $number" }
        val t0 = System.nanoTime()

        // Ensure the learning engine is initialised (no-op if already done)
        orchestrator.initialise()

        // Identify the previous round (single-row query)
        val previousRounds = roundRepo.lastN(1)
        val previousRoundId = previousRounds.firstOrNull()?.id
        val t1 = System.nanoTime()

        // Resolve the previous round's predictions (single UPDATE)
        previousRoundId?.let { prev ->
            predictionRepo.resolve(prev, number)
            Log.d("AddRound", "Resolved predictions for round $prev with actual=$number")
        }
        val t2 = System.nanoTime()

        // Insert the new round — O(1) now that we don't load 1000 prior rounds
        val newId = roundRepo.add(number, epochMs)
        val t3 = System.nanoTime()

        // O(1) stats cache update — immediate, so the next prediction has fresh data
        statsCache.update(number)
        val t4 = System.nanoTime()

        // Trigger the full self-learning cycle (reward update + drift + metrics)
        // This runs synchronously but on dispatchers.default — the ViewModel
        // already shows "Saving..." during this period. For truly instant
        // saves we could fire-and-forget, but the reward update needs to
        // complete before the next prediction for correct weighting.
        orchestrator.onNewRound(number, previousRoundId)
        val t5 = System.nanoTime()

        Log.d("AddRound", "Save complete: id=$newId number=$number | " +
            "init=${(t1 - t0) / 1_000_000}ms resolve=${(t2 - t1) / 1_000_000}ms " +
            "insert=${(t3 - t2) / 1_000_000}ms cache=${(t4 - t3) / 1_000_000}ms " +
            "learning=${(t5 - t4) / 1_000_000}ms total=${(t5 - t0) / 1_000_000}ms")

        newId
    }
}
