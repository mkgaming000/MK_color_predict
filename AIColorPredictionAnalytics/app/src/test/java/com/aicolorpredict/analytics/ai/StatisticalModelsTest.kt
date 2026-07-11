package com.aicolorpredict.analytics.ai

import com.aicolorpredict.analytics.ai.bayesian.BayesianNetworkModel
import com.aicolorpredict.analytics.ai.frequency.FrequencyAnalysisModel
import com.aicolorpredict.analytics.ai.markov.HiddenMarkovModel
import com.aicolorpredict.analytics.ai.markov.MarkovChainModel
import com.aicolorpredict.analytics.ai.monte.MonteCarloModel
import com.aicolorpredict.analytics.ai.movingavg.MovingAverageModel
import com.aicolorpredict.analytics.ai.transition.TransitionMatrixModel
import com.aicolorpredict.analytics.ai.ModelTestUtils
import com.aicolorpredict.analytics.ai.ModelTestUtils.runModel
import org.junit.Test

class StatisticalModelsTest {

    private val smallHistory = (1..200).map { (0..9).random() }
    private val bigHistory = (1..2000).map { (0..9).random() }

    @Test fun `frequency analysis produces well-formed output on small history`() =
        runModel(FrequencyAnalysisModel(), smallHistory)

    @Test fun `frequency analysis produces well-formed output on big history`() =
        runModel(FrequencyAnalysisModel(), bigHistory)

    @Test fun `markov chain produces well-formed output`() =
        runModel(MarkovChainModel(), smallHistory)

    @Test fun `hidden markov model produces well-formed output`() =
        runModel(HiddenMarkovModel(), smallHistory)

    @Test fun `bayesian network produces well-formed output`() =
        runModel(BayesianNetworkModel(), smallHistory)

    @Test fun `transition matrix produces well-formed output`() =
        runModel(TransitionMatrixModel(), smallHistory)

    @Test fun `moving average produces well-formed output`() =
        runModel(MovingAverageModel(), smallHistory)

    @Test fun `monte carlo produces well-formed output`() =
        runModel(MonteCarloModel(simulations = 200), smallHistory)

    @Test fun `monte carlo is deterministic for the same seed`() {
        val model = MonteCarloModel(simulations = 100, seed = 42)
        val fs1 = com.aicolorpredict.analytics.feature.FeatureEngineer().build(roundId = 1L, history = smallHistory)
        val fs2 = com.aicolorpredict.analytics.feature.FeatureEngineer().build(roundId = 1L, history = smallHistory)
        val o1 = kotlinx.coroutines.runBlocking { model.predict(fs1, smallHistory, 0.1) }
        val o2 = kotlinx.coroutines.runBlocking { model.predict(fs2, smallHistory, 0.1) }
        // Same seed + same roundId → same output (we feed roundId into the RNG)
        for (n in 0..9) {
            com.google.common.truth.Truth.assertThat(o1.numberProbabilities[n]!!)
                .isWithin(1e-9).of(o2.numberProbabilities[n]!!)
        }
    }

    @Test fun `models handle empty history gracefully`() {
        // With 0 history the Markov / Transition / Bayesian models should not throw.
        // Some may return uniform; the only contract is "no exception".
        runModel(MarkovChainModel(), emptyList())
        runModel(BayesianNetworkModel(), emptyList())
        runModel(FrequencyAnalysisModel(), emptyList())
    }

    @Test fun `confidence never exceeds the model's stated cap`() {
        // Statistical models cap at <= 0.85
        val models = listOf(
            FrequencyAnalysisModel(), MarkovChainModel(), HiddenMarkovModel(),
            TransitionMatrixModel(), BayesianNetworkModel(), MovingAverageModel(),
            MonteCarloModel(simulations = 200)
        )
        for (m in models) {
            val fs = com.aicolorpredict.analytics.feature.FeatureEngineer().build(1L, bigHistory)
            val o = kotlinx.coroutines.runBlocking { m.predict(fs, bigHistory, 0.1) }
            com.google.common.truth.Truth.assertThat(o.confidence).isAtMost(0.86)  // small tolerance
        }
    }
}
