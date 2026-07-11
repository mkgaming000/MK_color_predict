package com.aicolorpredict.analytics.ai.tree

/**
 * LightGBM-style variant — leaf-wise tree growth approximated by deeper
 * trees with smaller minimum split size, and a higher number of estimators
 * with a tiny learning rate. Designed to capture more granular splits than
 * the base GBM.
 */
class LightGbmModel : GradientBoostingModel(
    nEstimators = 20,
    maxDepth = 5,
    learningRate = 0.06,
    seed = 0xLGBM_42,
    modelDisplayName = "LightGBM"
)
