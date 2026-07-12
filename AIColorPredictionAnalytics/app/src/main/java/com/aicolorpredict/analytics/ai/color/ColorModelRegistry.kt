package com.aicolorpredict.analytics.ai.color

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry of all color-only AI models.
 */
@Singleton
class ColorModelRegistry @Inject constructor() {

    val models: List<ColorPredictionModel> = listOf(
        FrequencyColorModel(),
        MarkovColorModel(),
        BayesianColorModel(),
        MovingAverageColorModel(),
        TrendColorModel(),
        TransitionColorModel(),
        StreakColorModel(),
        AlternationColorModel()
    )

    val names: List<String> = models.map { it.name }
}
