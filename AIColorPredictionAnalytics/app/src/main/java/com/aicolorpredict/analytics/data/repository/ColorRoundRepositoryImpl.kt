package com.aicolorpredict.analytics.data.repository

import com.aicolorpredict.analytics.data.local.dao.ColorRoundDao
import com.aicolorpredict.analytics.data.local.entity.toDomain
import com.aicolorpredict.analytics.data.local.entity.toEntity
import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.domain.model.ColorRound
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorRoundRepositoryImpl @Inject constructor(
    private val dao: ColorRoundDao,
    private val dispatchers: AppDispatchers
) : ColorRoundRepository {

    override suspend fun add(color: AppColor, timestamp: Long?): Long = withContext(dispatchers.io) {
        val now = timestamp ?: System.currentTimeMillis()
        val prevRound = dao.getLastN(1).firstOrNull()
        val prevColor = if (prevRound != null) AppColor.fromCode(prevRound.color) else null
        val streak = if (prevRound != null && prevColor == color) prevRound.streak + 1 else 1
        val seqIdx = (dao.maxSequenceIndex() ?: -1) + 1
        val round = ColorRound(
            id = 0, timestamp = now, color = color,
            previousColor = prevColor, streak = streak, sequenceIndex = seqIdx
        )
        dao.insert(round.toEntity().copy(id = 0))
    }

    override suspend fun addMany(rounds: List<ColorRound>): List<Long> = withContext(dispatchers.io) {
        dao.insertAll(rounds.map { it.toEntity() })
    }

    override suspend fun lastN(n: Int): List<ColorRound> = withContext(dispatchers.io) {
        dao.getLastN(n).map { it.toDomain() }
    }

    override fun observeLastN(n: Int): Flow<List<ColorRound>> =
        dao.observeLastN(n).map { list -> list.map { it.toDomain() } }

    override suspend fun page(limit: Int, offset: Int): List<ColorRound> = withContext(dispatchers.io) {
        dao.getPage(limit, offset).map { it.toDomain() }
    }

    override suspend fun count(): Int = withContext(dispatchers.io) { dao.count() }

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override suspend fun clearAll() = withContext(dispatchers.io) { dao.clearAll() }

    override suspend fun all(): List<ColorRound> = withContext(dispatchers.io) {
        dao.getAll().map { it.toDomain() }
    }

    override suspend fun colorHistogram(): Map<Int, Int> = withContext(dispatchers.io) {
        dao.colorHistogram().associate { it.color to it.c }
    }

    override suspend fun maxSequenceIndex(): Int = withContext(dispatchers.io) {
        dao.maxSequenceIndex() ?: -1
    }
}
