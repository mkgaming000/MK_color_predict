package com.aicolorpredict.analytics.data.repository

import com.aicolorpredict.analytics.domain.model.Round
import kotlinx.coroutines.flow.Flow

interface RoundRepository {
    suspend fun add(number: Int, epochMs: Long? = null): Long
    suspend fun addMany(rounds: List<Round>): List<Long>
    suspend fun get(id: Long): Round?
    suspend fun lastN(n: Int): List<Round>
    fun observeLastN(n: Int): Flow<List<Round>>
    suspend fun page(limit: Int, offset: Int): List<Round>
    suspend fun count(): Int
    fun observeCount(): Flow<Int>
    suspend fun clearAll()
    suspend fun delete(id: Long)
    suspend fun byNumber(n: Int): List<Round>
    suspend fun byTimeRange(from: Long, to: Long): List<Round>
    suspend fun all(): List<Round>
    suspend fun numberHistogram(): Map<Int, Int>
}
