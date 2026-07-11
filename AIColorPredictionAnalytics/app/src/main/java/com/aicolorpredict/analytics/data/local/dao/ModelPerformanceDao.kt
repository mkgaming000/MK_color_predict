package com.aicolorpredict.analytics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aicolorpredict.analytics.data.local.entity.ModelPerformanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelPerformanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ModelPerformanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ModelPerformanceEntity>)

    @Query("SELECT * FROM model_performance ORDER BY top1Accuracy DESC")
    suspend fun getAll(): List<ModelPerformanceEntity>

    @Query("SELECT * FROM model_performance ORDER BY top1Accuracy DESC")
    fun observeAll(): Flow<List<ModelPerformanceEntity>>

    @Query("SELECT * FROM model_performance WHERE modelName = :name")
    suspend fun getByName(name: String): ModelPerformanceEntity?

    @Query("DELETE FROM model_performance")
    suspend fun clearAll()
}
