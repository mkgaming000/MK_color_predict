package com.aicolorpredict.analytics.data.repository

import com.aicolorpredict.analytics.data.local.dao.RoundDao
import com.aicolorpredict.analytics.data.local.entity.NumberCount
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
        val prior = dao.getLastN(1000).map { it.number }.reversed()
        val now = epochMs ?: System.currentTimeMillis()
        val round = Round.fromNumber(id = 0, epochMs = now, number = number, prior = prior)
        dao.insert(round.toEntity().copy(id = 0))
    }

    override suspend fun addMany(rounds: List<Round>): List<Long> = withContext(dispatchers.io) {
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
