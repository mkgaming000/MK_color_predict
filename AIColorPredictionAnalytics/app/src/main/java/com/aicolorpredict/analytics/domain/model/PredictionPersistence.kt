package com.aicolorpredict.analytics.domain.model

import com.aicolorpredict.analytics.data.local.entity.PredictionEntity

/**
 * Persistence helpers for [ModelOutput] / [Prediction]. The CSV strings on disk
 * are intentionally simple ("0=0.05,1=0.12,...") so they can be inspected by hand
 * and re-imported losslessly.
 */

fun ModelOutput.toEntity(roundId: Long): PredictionEntity = PredictionEntity(
    roundId = roundId,
    epochMs = System.currentTimeMillis(),
    modelName = modelName,
    topPick = topPick,
    topProbability = topProbability,
    confidence = confidence,
    reason = reason,
    numberProbabilities = numberProbabilities.entries.joinToString(",") { "${it.key}=${it.value}" },
    colorProbabilities = colorProbabilities.entries.joinToString(",") { "${it.key}=${it.value}" },
    actualOutcome = null,
    correct = null
)

fun PredictionEntity.toModelOutput(): ModelOutput {
    val np = numberProbabilities.split(',')
        .mapNotNull { e ->
            val (k, v) = e.split('=').let { it.getOrNull(0) to it.getOrNull(1) }
            k?.toIntOrNull()?.let { kk -> v?.toDoubleOrNull()?.let { vv -> kk to vv } }
        }.toMap()
    val cp = colorProbabilities.split(',')
        .mapNotNull { e ->
            val (k, v) = e.split('=').let { it.getOrNull(0) to it.getOrNull(1) }
            k?.let { kk -> v?.toDoubleOrNull()?.let { vv -> BallColor.valueOf(kk) to vv } }
        }.toMap()
    return ModelOutput(
        modelName = modelName,
        numberProbabilities = np,
        colorProbabilities = cp,
        confidence = confidence,
        reason = reason,
        accuracy = 0.0,  // not stored on prediction row; queried from performance table
        topPick = topPick,
        topProbability = topProbability
    )
}
