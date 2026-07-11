package com.aicolorpredict.analytics.ai.base

import com.aicolorpredict.analytics.ai.bayesian.BayesianNetworkModel
import com.aicolorpredict.analytics.ai.frequency.FrequencyAnalysisModel
import com.aicolorpredict.analytics.ai.markov.HiddenMarkovModel
import com.aicolorpredict.analytics.ai.markov.MarkovChainModel
import com.aicolorpredict.analytics.ai.monte.MonteCarloModel
import com.aicolorpredict.analytics.ai.movingavg.MovingAverageModel
import com.aicolorpredict.analytics.ai.neural.GruModel
import com.aicolorpredict.analytics.ai.neural.LstmModel
import com.aicolorpredict.analytics.ai.neural.TemporalCnnModel
import com.aicolorpredict.analytics.ai.neural.TransformerModel
import com.aicolorpredict.analytics.ai.transition.TransitionMatrixModel
import com.aicolorpredict.analytics.ai.tree.CatBoostModel
import com.aicolorpredict.analytics.ai.tree.LightGbmModel
import com.aicolorpredict.analytics.ai.tree.RandomForestModel
import com.aicolorpredict.analytics.ai.tree.XGBoostModel
import com.aicolorpredict.analytics.domain.model.ModelPerformance
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central registry of every [PredictionModel] in the system.
 *
 * Construction is cheap (each model is a small Kotlin object with no native
 * resources). The registry is a singleton so every prediction request goes
 * through the same set of models, and so the per-model [ConfidenceCalibrator]s
 * can be looked up by name.
 */
@Singleton
class ModelRegistry @Inject constructor(
    private val calibratorFactory: CalibratorFactory
) {
    /** The full ordered list of base models. Order is the display order in the UI. */
    val models: List<PredictionModel> = listOf(
        FrequencyAnalysisModel(),
        MarkovChainModel(),
        HiddenMarkovModel(),
        TransitionMatrixModel(),
        BayesianNetworkModel(),
        MovingAverageModel(),
        MonteCarloModel(),
        RandomForestModel(),
        XGBoostModel(),
        LightGbmModel(),
        CatBoostModel(),
        LstmModel(),
        GruModel(),
        TransformerModel(),
        TemporalCnnModel()
    )

    val names: List<String> = models.map { it.name }

    fun byName(name: String): PredictionModel? = models.firstOrNull { it.name == name }

    /** Per-model calibrators — lazily initialised. */
    fun calibratorFor(modelName: String) = calibratorFactory.get(modelName)
}

/** Keeps one [com.aicolorpredict.analytics.ai.calibration.ConfidenceCalibrator] per model name. */
@Singleton
class CalibratorFactory @Inject constructor() {
    private val map = mutableMapOf<String, com.aicolorpredict.analytics.ai.calibration.ConfidenceCalibrator>()
    fun get(name: String): com.aicolorpredict.analytics.ai.calibration.ConfidenceCalibrator =
        map.getOrPut(name) { com.aicolorpredict.analytics.ai.calibration.ConfidenceCalibrator() }
}
