package com.aicolorpredict.analytics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aicolorpredict.analytics.data.local.entity.PredictionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: PredictionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(predictions: List<PredictionEntity>)

    @Query("SELECT * FROM predictions WHERE roundId = :roundId ORDER BY modelName")
    suspend fun getByRound(roundId: Long): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE modelName = :model ORDER BY epochMs DESC LIMIT :limit")
    suspend fun getRecentByModel(model: String, limit: Int): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE modelName = :model AND correct IS NOT NULL ORDER BY epochMs DESC LIMIT :limit")
    suspend fun getRecentResolvedByModel(model: String, limit: Int): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE roundId = :roundId")
    fun observeByRound(roundId: Long): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE correct IS NULL ORDER BY epochMs ASC")
    suspend fun getUnresolved(): List<PredictionEntity>

    @Query("UPDATE predictions SET actualOutcome = :actual, correct = :correct WHERE roundId = :roundId")
    suspend fun resolve(roundId: Long, actual: Int, correct: Int)

    @Query("SELECT COUNT(*) FROM predictions WHERE modelName = :model AND correct IS NOT NULL")
    suspend fun resolvedCount(model: String): Int

    @Query("SELECT COUNT(*) FROM predictions WHERE modelName = :model AND correct = 1")
    suspend fun correctCount(model: String): Int

    @Query("DELETE FROM predictions")
    suspend fun clearAll()
}
