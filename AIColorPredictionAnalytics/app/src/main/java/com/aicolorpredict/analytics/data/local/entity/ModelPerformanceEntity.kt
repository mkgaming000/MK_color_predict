package com.aicolorpredict.analytics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Rolling per-model performance, persisted so the ensemble can pick up its
 * weights on cold start. The metrics are recomputed from the predictions table
 * periodically; this row is a cache.
 */
@Entity(tableName = "model_performance")
data class ModelPerformanceEntity(
    @PrimaryKey val modelName: String,
    val samplesObserved: Int,
    val top1Accuracy: Double,
    val top3Accuracy: Double,
    val top5Accuracy: Double,
    val logLoss: Double,
    val brierScore: Double,
    val precision: Double,
    val recall: Double,
    val f1: Double,
    val confusionMatrixCsv: String,
    val rollingAccuracy: Double,
    val rollingWindow: Int,
    val lastUpdated: Long
)
