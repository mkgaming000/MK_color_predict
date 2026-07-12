package com.aicolorpredict.analytics.data.local.entity

import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.domain.model.ColorModelOutput
import com.aicolorpredict.analytics.domain.model.ColorModelPerformance
import com.aicolorpredict.analytics.domain.model.ColorRound

fun ColorRoundEntity.toDomain(): ColorRound = ColorRound(
    id = id,
    timestamp = timestamp,
    color = AppColor.fromCode(color),
    previousColor = if (previousColor < 0) null else AppColor.fromCode(previousColor),
    streak = streak,
    sequenceIndex = sequenceIndex
)

fun ColorRound.toEntity(): ColorRoundEntity = ColorRoundEntity(
    id = id,
    timestamp = timestamp,
    color = color.code,
    previousColor = previousColor?.code ?: -1,
    streak = streak,
    sequenceIndex = sequenceIndex
)

fun ColorModelOutput.toEntity(roundId: Long): ColorPredictionEntity = ColorPredictionEntity(
    roundId = roundId,
    timestamp = System.currentTimeMillis(),
    modelName = modelName,
    topColor = topColor.code,
    topProbability = topProbability,
    redProbability = redProbability,
    greenProbability = greenProbability,
    confidence = confidence,
    reason = reason,
    actualColor = null,
    correct = null
)

fun ColorPredictionEntity.toModelOutput(): ColorModelOutput {
    return ColorModelOutput(
        modelName = modelName,
        redProbability = redProbability,
        greenProbability = greenProbability,
        confidence = confidence,
        reason = reason,
        accuracy = 0.0
    )
}

fun ColorModelPerformanceEntity.toDomain(): ColorModelPerformance = ColorModelPerformance(
    modelName = modelName,
    samplesObserved = samplesObserved,
    top1Accuracy = top1Accuracy,
    logLoss = logLoss,
    brierScore = brierScore,
    rollingAccuracy = rollingAccuracy,
    lastUpdated = lastUpdated
)

fun ColorModelPerformance.toEntity(): ColorModelPerformanceEntity = ColorModelPerformanceEntity(
    modelName = modelName,
    samplesObserved = samplesObserved,
    top1Accuracy = top1Accuracy,
    logLoss = logLoss,
    brierScore = brierScore,
    rollingAccuracy = rollingAccuracy,
    lastUpdated = lastUpdated
)
