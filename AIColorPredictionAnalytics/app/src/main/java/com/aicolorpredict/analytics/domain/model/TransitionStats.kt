package com.aicolorpredict.analytics.domain.model

/**
 * Aggregate stats for "given previous number X, what comes next?" — used by
 * the Transition Analysis screen and the TransitionMatrixModel.
 */
data class TransitionStats(
    val fromNumber: Int,
    val totalTransitions: Int,
    val nextNumberCounts: Map<Int, Int>,
    val nextNumberProbabilities: Map<Int, Double>,
    val nextColorCounts: Map<BallColor, Int>,
    val nextColorProbabilities: Map<BallColor, Double>,
    val averageGap: Map<Int, Double>,
    val historicalCount: Int
)
