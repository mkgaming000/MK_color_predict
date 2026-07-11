package com.aicolorpredict.analytics.ai

import com.aicolorpredict.analytics.ai.neural.GruModel
import com.aicolorpredict.analytics.ai.neural.LstmModel
import com.aicolorpredict.analytics.ai.neural.TemporalCnnModel
import com.aicolorpredict.analytics.ai.neural.TransformerModel
import com.aicolorpredict.analytics.ai.ModelTestUtils.runModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class NeuralModelsTest {

    private val history = (1..300).map { (0..9).random() }

    @Test fun `LSTM produces well-formed output`() = runModel(LstmModel(hiddenSize = 16), history)

    @Test fun `GRU produces well-formed output`() = runModel(GruModel(hiddenSize = 16), history)

    @Test fun `Transformer produces well-formed output`() = runModel(TransformerModel(dModel = 12, seqLen = 24), history)

    @Test fun `Temporal CNN produces well-formed output`() = runModel(TemporalCnnModel(channels = 12), history)

    @Test fun `LSTM handles short history without throwing`() = runModel(LstmModel(), listOf(1, 2, 3))

    @Test fun `LSTM handles empty history by returning uniform`() {
        val model = LstmModel()
        val fs = com.aicolorpredict.analytics.feature.FeatureEngineer().build(1L, emptyList())
        val out = runBlocking { model.predict(fs, emptyList(), 0.0) }
        assertThat(out.numberProbabilities.values.first()).isWithin(1e-9).of(0.1)
    }

    @Test fun `neural confidence stays below the 0_7 cap`() {
        val models = listOf(LstmModel(), GruModel(), TransformerModel(), TemporalCnnModel())
        for (m in models) {
            val fs = com.aicolorpredict.analytics.feature.FeatureEngineer().build(1L, history)
            val out = runBlocking { m.predict(fs, history, 0.1) }
            assertThat(out.confidence).isAtMost(0.71)
        }
    }
}
