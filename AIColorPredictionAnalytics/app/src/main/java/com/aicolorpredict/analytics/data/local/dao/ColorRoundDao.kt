package com.aicolorpredict.analytics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aicolorpredict.analytics.data.local.entity.ColorRoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorRoundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(round: ColorRoundEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rounds: List<ColorRoundEntity>): List<Long>

    @Query("SELECT * FROM color_rounds ORDER BY timestamp DESC LIMIT :n")
    suspend fun getLastN(n: Int): List<ColorRoundEntity>

    @Query("SELECT * FROM color_rounds ORDER BY timestamp DESC LIMIT :n")
    fun observeLastN(n: Int): Flow<List<ColorRoundEntity>>

    @Query("SELECT * FROM color_rounds ORDER BY timestamp ASC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<ColorRoundEntity>

    @Query("SELECT * FROM color_rounds ORDER BY timestamp ASC")
    suspend fun getAll(): List<ColorRoundEntity>

    @Query("SELECT * FROM color_rounds WHERE id = :id")
    suspend fun getById(id: Long): ColorRoundEntity?

    @Query("SELECT COUNT(*) FROM color_rounds")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM color_rounds")
    fun observeCount(): Flow<Int>

    @Query("SELECT color, COUNT(*) AS c FROM color_rounds GROUP BY color")
    suspend fun colorHistogram(): List<ColorCount>

    @Query("SELECT MAX(sequenceIndex) FROM color_rounds")
    suspend fun maxSequenceIndex(): Int?

    @Query("DELETE FROM color_rounds")
    suspend fun clearAll()

    @Query("DELETE FROM color_rounds WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class ColorCount(val color: Int, val c: Int)
