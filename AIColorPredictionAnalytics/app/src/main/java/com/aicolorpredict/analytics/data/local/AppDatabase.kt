package com.aicolorpredict.analytics.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aicolorpredict.analytics.data.local.dao.ColorModelPerformanceDao
import com.aicolorpredict.analytics.data.local.dao.ColorPredictionDao
import com.aicolorpredict.analytics.data.local.dao.ColorRoundDao
import com.aicolorpredict.analytics.data.local.entity.ColorModelPerformanceEntity
import com.aicolorpredict.analytics.data.local.entity.ColorPredictionEntity
import com.aicolorpredict.analytics.data.local.entity.ColorRoundEntity

@Database(
    entities = [
        ColorRoundEntity::class,
        ColorPredictionEntity::class,
        ColorModelPerformanceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun colorRoundDao(): ColorRoundDao
    abstract fun colorPredictionDao(): ColorPredictionDao
    abstract fun colorModelPerformanceDao(): ColorModelPerformanceDao

    companion object {
        const val NAME = "aicp.db"
    }
}
