package com.aicolorpredict.analytics.ai.tree

/**
 * CatBoost-style variant — symmetric tree approximated by depth-2 trees with
 * more estimators, mimicking CatBoost's "oblivious tree" structure. Lower
 * learning rate than the base GBM and more rounds to compensate.
 */
class CatBoostModel : GradientBoostingModel(
    nEstimators = 24,
    maxDepth = 2,
    learningRate = 0.05,
    seed = 0xCB_42,
    modelDisplayName = "CatBoost"
)
