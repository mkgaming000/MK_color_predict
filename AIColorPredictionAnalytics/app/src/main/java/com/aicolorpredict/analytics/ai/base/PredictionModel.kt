package com.aicolorpredict.analytics.ai.base

import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput

/**
 * Contract for every AI model in the system.
 *
 * Each implementation:
 *   - is *stateless across calls* — any state lives in the [FeatureSet] payload
 *     passed in, never in the model itself. This makes models trivially thread-safe
 *     and side-effect free.
 *   - returns a single [ModelOutput] whose numberProbabilities always sums to 1.0.
 *   - reports its own self-assessed [accuracy] separately from its confidence;
 *     the ensemble layer is responsible for combining them.
 *
 * The [predict] method is `suspend` so heavy models (e.g. LSTM) can yield.
 */
interface PredictionModel {
    val name: String
    val category: ModelCategory

    suspend fun predict(features: FeatureSet, history: List<Int>, rollingAccuracy: Double): ModelOutput
}

enum class ModelCategory(val display: String) {
    STATISTICAL("Statistical"),
    TREE("Tree-based"),
    NEURAL("Neural"),
    ENSEMBLE("Ensemble")
}
