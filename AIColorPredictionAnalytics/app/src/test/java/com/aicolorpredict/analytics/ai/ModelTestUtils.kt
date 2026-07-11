package com.aicolorpredict.analytics.ai

import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.feature.FeatureEngineer
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Shared test helpers for the AI model tests.
 */
object ModelTestUtils {

    /** Builds a synthetic history by repeatedly sampling from a biased RNG. */
    fun biasedHistory(rounds: Int, bias: Int = 7): List<Int> =
        (1..rounds).map { if ((1..10).random() <= 7) bias else (0..9).random() }

    /** Plain uniform history. */
    fun uniformHistory(rounds: Int): List<Int> = (1..rounds).map { (0..9).random() }

    /** Asserts that a model output is well-formed: 10 entries, sums to 1, finite, non-negative. */
    fun assertWellFormed(modelName: String, output: com.aicolorpredict.analytics.domain.model.ModelOutput) {
        assertThat(output.numberProbabilities.size).isEqualTo(10)
        val sum = output.numberProbabilities.values.sum()
        assertThat(sum).isWithin(1e-6).of(1.0)
        output.numberProbabilities.values.forEach { p ->
            assertThat(p).isAtLeast(0.0)
            assertThat(p.isFinite()).isTrue()
        }
        assertThat(output.confidence).isAtLeast(0.0)
        assertThat(output.confidence).isAtMost(1.0)
        assertThat(output.reason).contains(modelName)
    }

    /** Runs a model end-to-end against a history and asserts well-formed output. */
    fun runModel(model: PredictionModel, history: List<Int>) {
        val fs = FeatureEngineer().build(roundId = 1L, history = history)
        val output = runBlocking { model.predict(fs, history, rollingAccuracy = 0.1) }
        assertWellFormed(model.name, output)
    }
}
