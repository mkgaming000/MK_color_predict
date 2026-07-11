package com.aicolorpredict.analytics.domain.usecase

import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.feature.TransitionAnalytics
import com.aicolorpredict.analytics.domain.model.TransitionStats
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val roundRepo: RoundRepository,
    private val dispatchers: AppDispatchers
) {
    suspend fun byRoundId(id: Long): Round? = withContext(dispatchers.io) { roundRepo.get(id) }

    suspend fun byNumber(n: Int): List<Round> = withContext(dispatchers.io) { roundRepo.byNumber(n) }

    suspend fun byDateRange(from: Long, to: Long): List<Round> = withContext(dispatchers.io) {
        roundRepo.byTimeRange(from, to)
    }

    /**
     * Search for rounds matching a pattern (e.g. "3,7" matches any round whose
     * recent window ends with 3 then 7).
     */
    suspend fun byPattern(pattern: List<Int>): List<Round> = withContext(dispatchers.io) {
        if (pattern.isEmpty()) return@withContext emptyList()
        val all = roundRepo.all()
        all.filter { r ->
            // Match against the previous10 + current number
            val window = r.previous10 + r.number
            // Search for the pattern as a contiguous subsequence
            if (window.size < pattern.size) false
            else (0..window.size - pattern.size).any { i ->
                window.subList(i, i + pattern.size) == pattern
            }
        }
    }

    /** Returns the [TransitionStats] for a given previous number. */
    suspend fun transitionStats(from: Int): TransitionStats = withContext(dispatchers.default) {
        val history = roundRepo.lastN(2000).map { it.number }
        TransitionAnalytics.build(from, history)
    }
}
