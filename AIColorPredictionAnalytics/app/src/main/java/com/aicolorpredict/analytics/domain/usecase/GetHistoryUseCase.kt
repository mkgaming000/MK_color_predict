package com.aicolorpredict.analytics.domain.usecase

import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val dispatchers: AppDispatchers
) {
    /** Recent rounds as a Flow for the History screen. */
    fun observeRecent(limit: Int): Flow<List<Round>> = roundRepo.observeLastN(limit)

    /** Pull predictions associated with a given round for the History detail screen. */
    suspend fun predictionsFor(roundId: Long) = withContext(dispatchers.io) {
        predictionRepo.getByRound(roundId)
    }

    suspend fun page(limit: Int, offset: Int): List<Round> = withContext(dispatchers.io) {
        roundRepo.page(limit, offset)
    }

    suspend fun count(): Int = withContext(dispatchers.io) { roundRepo.count() }
}
