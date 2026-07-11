package com.aicolorpredict.analytics.domain.usecase

import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Adds a new round to the database and resolves any unresolved predictions
 * for the *previous* round (now that we know what came after it).
 *
 * This is the only entry point that mutates the rounds table from the UI.
 */
class AddRoundUseCase @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val dispatchers: AppDispatchers
) {
    /**
     * @param number the actual number that just came up (0..9)
     * @param epochMs optional timestamp; defaults to now
     * @return the new round's row id
     */
    suspend operator fun invoke(number: Int, epochMs: Long? = null): Long = withContext(dispatchers.default) {
        require(number in 0..9) { "Number must be 0..9, was $number" }
        // Resolve any outstanding predictions for the previous round before
        // adding the new one. The "previous" round here is whatever the most
        // recent round in the DB is — that's the round whose outcome was
        // unknown until now.
        val previousRounds = roundRepo.lastN(1)
        previousRounds.firstOrNull()?.let { prev ->
            predictionRepo.resolve(prev.id, number)
        }
        roundRepo.add(number, epochMs)
    }
}
