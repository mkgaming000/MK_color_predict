package com.aicolorpredict.analytics.data.repository

import com.aicolorpredict.analytics.data.local.dao.ModelPerformanceDao
import com.aicolorpredict.analytics.data.local.dao.PredictionDao
import com.aicolorpredict.analytics.data.local.entity.ModelPerformanceEntity
import com.aicolorpredict.analytics.data.local.entity.PredictionEntity
import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.aicolorpredict.analytics.domain.model.ModelPerformance
import com.aicolorpredict.analytics.domain.model.toEntity
import com.aicolorpredict.analytics.domain.model.toModelOutput
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionRepositoryImpl @Inject constructor(
    private val predictionDao: PredictionDao,
    private val performanceDao: ModelPerformanceDao,
    private val dispatchers: AppDispatchers
) : PredictionRepository {

    override suspend fun save(roundId: Long, outputs: List<ModelOutput>): List<Long> =
        withContext(dispatchers.io) {
            outputs.map { output ->
                predictionDao.insert(output.toEntity(roundId))
            }
        }

    override suspend fun getByRound(roundId: Long): List<ModelOutput> = withContext(dispatchers.io) {
        predictionDao.getByRound(roundId).map { it.toModelOutput() }
    }

    override suspend fun resolve(roundId: Long, actual: Int) = withContext(dispatchers.io) {
        // The UPDATE sets actualOutcome + per-row correct (CASE on topPick) for
        // ALL prediction rows with this roundId in a single round-trip.
        predictionDao.resolve(roundId, actual)
    }

    override suspend fun unresolved(): List<Pair<Long, Long>> = withContext(dispatchers.io) {
        predictionDao.getUnresolved().map { it.roundId to it.id }
    }

    override fun observeModels(): Flow<List<ModelPerformance>> =
        performanceDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun modelPerformance(model: String): ModelPerformance? =
        withContext(dispatchers.io) { performanceDao.getByName(model)?.toDomain() }

    override suspend fun allModelPerformance(): List<ModelPerformance> = withContext(dispatchers.io) {
        performanceDao.getAll().map { it.toDomain() }
    }

    override suspend fun upsertPerformance(p: ModelPerformance) = withContext(dispatchers.io) {
        performanceDao.upsert(p.toEntity())
    }

    override suspend fun upsertAllPerformance(list: List<ModelPerformance>) = withContext(dispatchers.io) {
        performanceDao.upsertAll(list.map { it.toEntity() })
    }

    override suspend fun clearAll() = withContext(dispatchers.io) {
        predictionDao.clearAll()
        performanceDao.clearAll()
    }

    override suspend fun recentResolvedByModel(model: String, limit: Int): List<Pair<ModelOutput, Int>> =
        withContext(dispatchers.io) {
            predictionDao.getRecentResolvedByModel(model, limit).map { it.toModelOutput() to (it.actualOutcome ?: -1) }
        }
}

private fun ModelPerformanceEntity.toDomain(): ModelPerformance = ModelPerformance(
    modelName = modelName,
    samplesObserved = samplesObserved,
    top1Accuracy = top1Accuracy,
    top3Accuracy = top3Accuracy,
    top5Accuracy = top5Accuracy,
    logLoss = logLoss,
    brierScore = brierScore,
    precision = precision,
    recall = recall,
    f1 = f1,
    confusionMatrix = parseConfusion(confusionMatrixCsv),
    rollingAccuracy = rollingAccuracy,
    rollingWindow = rollingWindow,
    lastUpdated = lastUpdated
)

private fun ModelPerformance.toEntity(): ModelPerformanceEntity = ModelPerformanceEntity(
    modelName = modelName,
    samplesObserved = samplesObserved,
    top1Accuracy = top1Accuracy,
    top3Accuracy = top3Accuracy,
    top5Accuracy = top5Accuracy,
    logLoss = logLoss,
    brierScore = brierScore,
    precision = precision,
    recall = recall,
    f1 = f1,
    confusionMatrixCsv = confusionMatrix.entries.joinToString(";") { (k, v) ->
        "$k:" + v.entries.joinToString(",") { (kk, vv) -> "$kk=$vv" }
    },
    rollingAccuracy = rollingAccuracy,
    rollingWindow = rollingWindow,
    lastUpdated = lastUpdated
)

private fun parseConfusion(csv: String): Map<Int, Map<Int, Int>> {
    if (csv.isBlank()) return emptyMap()
    return csv.split(';').mapNotNull { outer ->
        val (k, v) = outer.split(':', limit = 2).let { it.getOrNull(0) to it.getOrNull(1) }
        val key = k?.toIntOrNull() ?: return@mapNotNull null
        val inner = v?.split(',')?.mapNotNull { e ->
            val (kk, vv) = e.split('=', limit = 2).let { it.getOrNull(0) to it.getOrNull(1) }
            kk?.toIntOrNull()?.let { a -> vv?.toIntOrNull()?.let { b -> a to b } }
        }?.toMap() ?: emptyMap()
        key to inner
    }.toMap()
}
