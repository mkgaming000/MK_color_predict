package com.aicolorpredict.analytics.domain.model

import com.aicolorpredict.analytics.data.local.entity.PredictionEntity

/**
 * Persistence helpers for [ModelOutput] / [Prediction]. The CSV strings on disk
 * are intentionally simple ("0=0.05,1=0.12,...") so they can be inspected by hand
 * and re-imported losslessly.
 *
 * [toModelOutput] is defensive: if the persisted CSV is incomplete (e.g. legacy
 * or hand-edited), it back-fills missing numbers with zero probability and
 * re-normalises so the [ModelOutput] invariant (sum == 1.0) always holds.
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
    // Parse number probabilities defensively — back-fill missing entries with 0.
    val np = DoubleArray(10)
    if (numberProbabilities.isNotBlank()) {
        numberProbabilities.split(',').forEach { e ->
            val parts = e.split('=')
            val k = parts.getOrNull(0)?.trim()?.toIntOrNull()
            val v = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            if (k != null && k in 0..9 && v != null && v.isFinite() && v >= 0) np[k] = v
        }
    }
    // Ensure non-zero sum so fromVector doesn't fall back to uniform — we want
    // to preserve the actual stored distribution, not silently replace it.
    val sum = np.sum()
    val raw = if (sum <= 0.0) DoubleArray(10) { 0.1 } else np.map { it / sum }.toDoubleArray()

    // Parse color probabilities defensively.
    val cp = mutableMapOf<BallColor, Double>()
    if (colorProbabilities.isNotBlank()) {
        colorProbabilities.split(',').forEach { e ->
            val parts = e.split('=')
            val k = parts.getOrNull(0)?.trim()
            val v = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            if (k != null && v != null && v.isFinite()) {
                runCatching { BallColor.valueOf(k) }.getOrNull()?.let { bc -> cp[bc] = v }
            }
        }
    }
    // Back-fill missing colors with 0 so downstream consumers don't NPE.
    BallColor.entries.forEach { c -> cp.putIfAbsent(c, 0.0) }

    // Use fromVector to guarantee the sum-to-1 invariant; override topPick/
    // topProbability afterward from the stored values.
    return ModelOutput.fromVector(
        modelName = modelName,
        raw = raw,
        confidence = confidence.coerceIn(0.0, 1.0),
        reason = reason,
        accuracy = 0.0  // not stored on prediction row; queried from performance table
    ).copy(
        topPick = topPick.coerceIn(0, 9),
        topProbability = topProbability.coerceIn(0.0, 1.0),
        colorProbabilities = cp
    )
}
