package com.aicolorpredict.analytics.data.repository

import com.aicolorpredict.analytics.domain.model.AccuracyMetrics
import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.aicolorpredict.analytics.domain.model.ModelPerformance
import com.aicolorpredict.analytics.domain.model.Prediction
import kotlinx.coroutines.flow.Flow

interface PredictionRepository {
    suspend fun save(roundId: Long, outputs: List<ModelOutput>): List<Long>
    suspend fun getByRound(roundId: Long): List<ModelOutput>
    suspend fun resolve(roundId: Long, actual: Int)
    suspend fun unresolved(): List<Pair<Long, Long>> // roundId, predictionId
    fun observeModels(): Flow<List<ModelPerformance>>
    suspend fun modelPerformance(model: String): ModelPerformance?
    suspend fun allModelPerformance(): List<ModelPerformance>
    suspend fun upsertPerformance(p: ModelPerformance)
    suspend fun upsertAllPerformance(list: List<ModelPerformance>)
    suspend fun clearAll()
    suspend fun recentResolvedByModel(model: String, limit: Int): List<Pair<ModelOutput, Int>> // (output, actual)
}
