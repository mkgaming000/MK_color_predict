package com.aicolorpredict.analytics.data.repository

import com.aicolorpredict.analytics.domain.model.ColorModelOutput
import com.aicolorpredict.analytics.domain.model.ColorModelPerformance
import com.aicolorpredict.analytics.domain.model.ColorRound
import kotlinx.coroutines.flow.Flow

interface ColorRoundRepository {
    suspend fun add(color: com.aicolorpredict.analytics.domain.model.AppColor, timestamp: Long? = null): Long
    suspend fun addMany(rounds: List<ColorRound>): List<Long>
    suspend fun lastN(n: Int): List<ColorRound>
    fun observeLastN(n: Int): Flow<List<ColorRound>>
    suspend fun page(limit: Int, offset: Int): List<ColorRound>
    suspend fun count(): Int
    fun observeCount(): Flow<Int>
    suspend fun clearAll()
    suspend fun all(): List<ColorRound>
    suspend fun colorHistogram(): Map<Int, Int>
    suspend fun maxSequenceIndex(): Int
}

interface ColorPredictionRepository {
    suspend fun save(roundId: Long, outputs: List<ColorModelOutput>)
    suspend fun getByRound(roundId: Long): List<ColorModelOutput>
    suspend fun resolve(roundId: Long, actualColorCode: Int)
    suspend fun recentResolvedByModel(model: String, limit: Int): List<Pair<ColorModelOutput, Int>>
    suspend fun allModelPerformance(): List<ColorModelPerformance>
    suspend fun upsertAllPerformance(list: List<ColorModelPerformance>)
    suspend fun clearAll()
}
