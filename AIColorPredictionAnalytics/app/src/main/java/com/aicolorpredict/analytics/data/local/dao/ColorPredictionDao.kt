package com.aicolorpredict.analytics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aicolorpredict.analytics.data.local.entity.ColorModelPerformanceEntity
import com.aicolorpredict.analytics.data.local.entity.ColorPredictionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorPredictionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: ColorPredictionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(predictions: List<ColorPredictionEntity>)

    @Query("SELECT * FROM color_predictions WHERE roundId = :roundId ORDER BY modelName")
    suspend fun getByRound(roundId: Long): List<ColorPredictionEntity>

    @Query("SELECT * FROM color_predictions WHERE modelName = :model AND correct IS NOT NULL ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentResolvedByModel(model: String, limit: Int): List<ColorPredictionEntity>

    @Query("UPDATE color_predictions SET actualColor = :actual, correct = CASE WHEN topColor = :actual THEN 1 ELSE 0 END WHERE roundId = :roundId")
    suspend fun resolve(roundId: Long, actual: Int)

    @Query("DELETE FROM color_predictions")
    suspend fun clearAll()
}

@Dao
interface ColorModelPerformanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ColorModelPerformanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ColorModelPerformanceEntity>)

    @Query("SELECT * FROM color_model_performance ORDER BY top1Accuracy DESC")
    suspend fun getAll(): List<ColorModelPerformanceEntity>

    @Query("SELECT * FROM color_model_performance ORDER BY top1Accuracy DESC")
    fun observeAll(): Flow<List<ColorModelPerformanceEntity>>

    @Query("SELECT * FROM color_model_performance WHERE modelName = :name")
    suspend fun getByName(name: String): ColorModelPerformanceEntity?

    @Query("DELETE FROM color_model_performance")
    suspend fun clearAll()
}
