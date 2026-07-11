package com.aicolorpredict.analytics.ai

import com.aicolorpredict.analytics.ai.tree.CatBoostModel
import com.aicolorpredict.analytics.ai.tree.LightGbmModel
import com.aicolorpredict.analytics.ai.tree.RandomForestModel
import com.aicolorpredict.analytics.ai.tree.XGBoostModel
import com.aicolorpredict.analytics.ai.ModelTestUtils.runModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TreeModelsTest {

    private val history = (1..300).map { (0..9).random() }

    @Test fun `Random Forest produces well-formed output`() = runModel(RandomForestModel(nTrees = 8), history)

    @Test fun `XGBoost produces well-formed output`() = runModel(XGBoostModel(), history)

    @Test fun `LightGBM produces well-formed output`() = runModel(LightGbmModel(), history)

    @Test fun `CatBoost produces well-formed output`() = runModel(CatBoostModel(), history)

    @Test fun `tree models handle insufficient history by returning uniform`() {
        val models = listOf(RandomForestModel(), XGBoostModel(), LightGbmModel(), CatBoostModel())
        for (m in models) {
            val fs = com.aicolorpredict.analytics.feature.FeatureEngineer().build(1L, listOf(1, 2))
            val out = runBlocking { m.predict(fs, listOf(1, 2), 0.0) }
            // Top probability is uniform (0.1)
            assertThat(out.numberProbabilities.values.first()).isWithin(1e-9).of(0.1)
        }
    }

    @Test fun `tree confidence stays below 0_79`() {
        val models = listOf(RandomForestModel(), XGBoostModel(), LightGbmModel(), CatBoostModel())
        for (m in models) {
            val fs = com.aicolorpredict.analytics.feature.FeatureEngineer().build(1L, history)
            val out = runBlocking { m.predict(fs, history, 0.1) }
            assertThat(out.confidence).isAtMost(0.80)
        }
    }
}
