package com.aicolorpredict.analytics.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One color prediction made by one model for one round.
 */
@Entity(
    tableName = "color_predictions",
    indices = [
        Index(value = ["roundId"]),
        Index(value = ["modelName"]),
        Index(value = ["timestamp"]),
        Index(value = ["correct"])
    ]
)
data class ColorPredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val roundId: Long,
    val timestamp: Long,
    val modelName: String,
    val topColor: Int,           // 0 = RED, 1 = GREEN
    val topProbability: Double,
    val redProbability: Double,
    val greenProbability: Double,
    val confidence: Double,
    val reason: String,
    val actualColor: Int?,       // null until resolved
    val correct: Int?            // 0/1, null until resolved
)

@Entity(tableName = "color_model_performance")
data class ColorModelPerformanceEntity(
    @PrimaryKey val modelName: String,
    val samplesObserved: Int,
    val top1Accuracy: Double,
    val logLoss: Double,
    val brierScore: Double,
    val rollingAccuracy: Double,
    val lastUpdated: Long
)
