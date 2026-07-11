package com.aicolorpredict.analytics.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aicolorpredict.analytics.data.local.dao.ModelPerformanceDao
import com.aicolorpredict.analytics.data.local.dao.PredictionDao
import com.aicolorpredict.analytics.data.local.dao.RoundDao
import com.aicolorpredict.analytics.data.local.entity.ModelPerformanceEntity
import com.aicolorpredict.analytics.data.local.entity.PredictionEntity
import com.aicolorpredict.analytics.data.local.entity.RoundEntity

@Database(
    entities = [
        RoundEntity::class,
        PredictionEntity::class,
        ModelPerformanceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roundDao(): RoundDao
    abstract fun predictionDao(): PredictionDao
    abstract fun modelPerformanceDao(): ModelPerformanceDao

    companion object {
        const val NAME = "aicp.db"
    }
}
