package com.aicolorpredict.analytics.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One prediction made by one model for one round.
 *
 * [topPick] / [topProbability] are denormalised for fast dashboard queries.
 * [numberProbabilities] and [colorProbabilities] are stored as CSV strings
 * ("0=0.05,1=0.12,...") so we can avoid a separate probability table.
 *
 * [correct] is null until the actual outcome is observed; it is set to 0/1 by the
 * UpdateModelPerformanceUseCase.
 */
@Entity(
    tableName = "predictions",
    indices = [
        Index(value = ["roundId"]),
        Index(value = ["modelName"]),
        Index(value = ["epochMs"]),
        Index(value = ["correct"])
    ]
)
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val roundId: Long,
    val epochMs: Long,
    val modelName: String,
    val topPick: Int,
    val topProbability: Double,
    val confidence: Double,
    val reason: String,
    val numberProbabilities: String,   // "0=0.05,1=0.12,..."
    val colorProbabilities: String,    // "RED=0.45,GREEN=0.50,VIOLET=0.05"
    val actualOutcome: Int?,
    val correct: Int?                  // 0/1, null until resolved
)
