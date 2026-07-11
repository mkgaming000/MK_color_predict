package com.aicolorpredict.analytics.ai.tree

/**
 * XGBoost-style variant of [GradientBoostingModel].
 *
 * Differences from the base GBM (mostly cosmetic, since the underlying engine
 * is the same pure-Kotlin implementation):
 *   - Slightly deeper trees (maxDepth = 4)
 *   - Higher number of estimators (16)
 *   - Lower learning rate (0.08)
 *   - Different seed offset so the bootstrap samples differ from GBM
 *
 * In a production setting you would ship a real XGBoost AAR and call into it
 * here — the rest of the app does not care which engine produces the
 * [com.aicolorpredict.analytics.domain.model.ModelOutput].
 */
class XGBoostModel : GradientBoostingModel(
    nEstimators = 16,
    maxDepth = 4,
    learningRate = 0.08,
    seed = 6789012345L,
    modelDisplayName = "XGBoost"
)
