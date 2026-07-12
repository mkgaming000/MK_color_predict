package com.aicolorpredict.analytics.data.repository

import com.aicolorpredict.analytics.data.local.dao.ColorModelPerformanceDao
import com.aicolorpredict.analytics.data.local.dao.ColorPredictionDao
import com.aicolorpredict.analytics.data.local.entity.toDomain
import com.aicolorpredict.analytics.data.local.entity.toEntity
import com.aicolorpredict.analytics.data.local.entity.toModelOutput
import com.aicolorpredict.analytics.domain.model.ColorModelOutput
import com.aicolorpredict.analytics.domain.model.ColorModelPerformance
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorPredictionRepositoryImpl @Inject constructor(
    private val predictionDao: ColorPredictionDao,
    private val performanceDao: ColorModelPerformanceDao,
    private val dispatchers: AppDispatchers
) : ColorPredictionRepository {

    override suspend fun save(roundId: Long, outputs: List<ColorModelOutput>) = withContext(dispatchers.io) {
        predictionDao.insertAll(outputs.map { it.toEntity(roundId) })
    }

    override suspend fun getByRound(roundId: Long): List<ColorModelOutput> = withContext(dispatchers.io) {
        predictionDao.getByRound(roundId).map { it.toModelOutput() }
    }

    override suspend fun resolve(roundId: Long, actualColorCode: Int) = withContext(dispatchers.io) {
        predictionDao.resolve(roundId, actualColorCode)
    }

    override suspend fun recentResolvedByModel(model: String, limit: Int): List<Pair<ColorModelOutput, Int>> =
        withContext(dispatchers.io) {
            predictionDao.getRecentResolvedByModel(model, limit).map {
                it.toModelOutput() to (it.actualColor ?: -1)
            }
        }

    override suspend fun allModelPerformance(): List<ColorModelPerformance> = withContext(dispatchers.io) {
        performanceDao.getAll().map { it.toDomain() }
    }

    override suspend fun upsertAllPerformance(list: List<ColorModelPerformance>) = withContext(dispatchers.io) {
        performanceDao.upsertAll(list.map { it.toEntity() })
    }

    override suspend fun clearAll() = withContext(dispatchers.io) {
        predictionDao.clearAll()
        performanceDao.clearAll()
    }
}
