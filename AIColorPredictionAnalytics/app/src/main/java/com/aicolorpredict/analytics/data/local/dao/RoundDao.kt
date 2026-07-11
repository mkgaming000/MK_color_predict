package com.aicolorpredict.analytics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aicolorpredict.analytics.data.local.entity.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(round: RoundEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rounds: List<RoundEntity>): List<Long>

    @Query("SELECT * FROM rounds ORDER BY epochMs ASC")
    suspend fun getAll(): List<RoundEntity>

    @Query("SELECT * FROM rounds ORDER BY epochMs ASC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<RoundEntity>

    @Query("SELECT * FROM rounds ORDER BY epochMs DESC LIMIT :n")
    suspend fun getLastN(n: Int): List<RoundEntity>

    @Query("SELECT * FROM rounds ORDER BY epochMs DESC LIMIT :n")
    fun observeLastN(n: Int): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE id = :id")
    suspend fun getById(id: Long): RoundEntity?

    @Query("SELECT * FROM rounds WHERE number = :n ORDER BY epochMs DESC")
    suspend fun getByNumber(n: Int): List<RoundEntity>

    @Query("SELECT * FROM rounds WHERE epochMs BETWEEN :from AND :to ORDER BY epochMs ASC")
    suspend fun getByTimeRange(from: Long, to: Long): List<RoundEntity>

    @Query("SELECT COUNT(*) FROM rounds")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM rounds")
    fun observeCount(): Flow<Int>

    @Query("SELECT number, COUNT(*) AS c FROM rounds GROUP BY number ORDER BY number")
    suspend fun numberHistogram(): List<NumberCount>

    @Query("DELETE FROM rounds")
    suspend fun clearAll()

    @Query("DELETE FROM rounds WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT MAX(epochMs) FROM rounds")
    suspend fun latestEpoch(): Long?
}

data class NumberCount(val number: Int, val c: Int)
