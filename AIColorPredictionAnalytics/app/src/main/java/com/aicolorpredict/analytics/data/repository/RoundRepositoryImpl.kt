package com.aicolorpredict.analytics.data.repository

import com.aicolorpredict.analytics.data.local.dao.RoundDao
import com.aicolorpredict.analytics.data.local.dao.NumberCount
import com.aicolorpredict.analytics.data.local.entity.toDomain
import com.aicolorpredict.analytics.data.local.entity.toEntity
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoundRepositoryImpl @Inject constructor(
    private val dao: RoundDao,
    private val dispatchers: AppDispatchers
) : RoundRepository {

    override suspend fun add(number: Int, epochMs: Long?): Long = withContext(dispatchers.io) {
        // PERFORMANCE: Previously this loaded the last 1000 rounds from the DB
        // on every save to build the previousWindows. That's O(n) per save and
        // the single biggest bottleneck for data entry latency.
        //
        // Now we only need the *previous number* for the entity's previousNumber
        // field. The full window lists are built lazily — they're only needed
        // for display, not for the AI pipeline (which uses IncrementalStatsCache).
        //
        // This reduces save from O(n) to O(1) DB queries.
        val now = epochMs ?: System.currentTimeMillis()
        val lastRound = dao.getLastN(1).firstOrNull()
        val prior = if (lastRound != null) listOf(lastRound.number) else emptyList()
        val round = Round.fromNumber(id = 0, epochMs = now, number = number, prior = prior)
        dao.insert(round.toEntity().copy(id = 0))
    }

    override suspend fun addMany(rounds: List<Round>): List<Long> = withContext(dispatchers.io) {
        // Batch insert — single transaction for the entire list.
        dao.insertAll(rounds.map { it.toEntity() })
    }

    override suspend fun get(id: Long): Round? = withContext(dispatchers.io) {
        dao.getById(id)?.toDomain()
    }

    override suspend fun lastN(n: Int): List<Round> = withContext(dispatchers.io) {
        dao.getLastN(n).map { it.toDomain() }
    }

    override fun observeLastN(n: Int): Flow<List<Round>> =
        dao.observeLastN(n).map { list -> list.map { it.toDomain() } }

    override suspend fun page(limit: Int, offset: Int): List<Round> = withContext(dispatchers.io) {
        dao.getPage(limit, offset).map { it.toDomain() }
    }

    override suspend fun count(): Int = withContext(dispatchers.io) { dao.count() }

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override suspend fun clearAll() = withContext(dispatchers.io) { dao.clearAll() }

    override suspend fun delete(id: Long) = withContext(dispatchers.io) { dao.deleteById(id) }

    override suspend fun byNumber(n: Int): List<Round> = withContext(dispatchers.io) {
        dao.getByNumber(n).map { it.toDomain() }
    }

    override suspend fun byTimeRange(from: Long, to: Long): List<Round> = withContext(dispatchers.io) {
        dao.getByTimeRange(from, to).map { it.toDomain() }
    }

    override suspend fun all(): List<Round> = withContext(dispatchers.io) {
        dao.getAll().map { it.toDomain() }
    }

    override suspend fun numberHistogram(): Map<Int, Int> = withContext(dispatchers.io) {
        val raw: List<NumberCount> = dao.numberHistogram()
        raw.associate { it.number to it.c }
    }
}
