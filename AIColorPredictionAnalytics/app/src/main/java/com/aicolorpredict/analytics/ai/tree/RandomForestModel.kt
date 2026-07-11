package com.aicolorpredict.analytics.ai.tree

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random

/**
 * Random Forest — ensemble of [DecisionTree]s, each trained on a bootstrap
 * sample of the rolling training set. Predictions are averaged.
 *
 * On this size of problem (10-class classification, ~200 training samples) the
 * forest cannot match a desktop-trained scikit-learn model, but it provides a
 * genuinely different signal from the statistical / neural models, which is
 * exactly what the ensemble layer needs.
 *
 * Confidence is calibrated from the agreement between trees (high agreement =
 * high confidence) and from the magnitude of the top probability.
 */
class RandomForestModel(
    private val nTrees: Int = 16,
    private val maxDepth: Int = 5,
    private val seed: Long = 8901234567L
) : PredictionModel {
    override val name: String = "Random Forest"
    override val category: ModelCategory = ModelCategory.TREE

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        if (history.size < 30) return uniform("Insufficient history for Random Forest.")
        val training = buildTreeTrainingSet(history, windowSize = 50, maxSamples = 200)
        if (training.isEmpty()) return uniform("Empty training set for Random Forest.")

        val forest = (0 until nTrees).map { i ->
            val rng = Random(seed + i)
            val tree = DecisionTree(maxDepth = maxDepth, minSamplesSplit = 4, featureSamplingRate = 0.3, rng = rng)
            val sample = List(training.size) { training[rng.nextInt(training.size)] }
            tree.fit(sample)
            tree
        }

        // Predict on the *current* feature vector. We use the quickFeatures
        // extractor to keep the feature space consistent with training.
        val currentWindow = history.takeLast(50)
        val x = quickFeatures(currentWindow)
        val aggregated = DoubleArray(10)
        for (tree in forest) {
            val p = tree.predict(x)
            for (j in 0..9) aggregated[j] += p[j]
        }
        for (j in 0..9) aggregated[j] /= nTrees
        val probs = ModelUtils.normalise(aggregated)

        val top = probs.indices.maxByOrNull { probs[it] } ?: 0
        val topProb = probs[top]

        // Tree agreement: how many trees picked the same top?
        val treeVotes = IntArray(10)
        for (tree in forest) {
            val p = tree.predict(x)
            treeVotes[p.indices.maxByOrNull { p[it] } ?: 0]++
        }
        val agreement = treeVotes.maxOrNull()?.toDouble()?.div(nTrees) ?: 0.0

        val concentration = probs.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val confidence = ((1.0 - exp(-concentration)) * (0.4 + 0.5 * agreement)).coerceIn(0.0, 0.78)

        val evidence = buildString {
            append("Random Forest of $nTrees trees (depth $maxDepth); ")
            append("training samples: ${training.size}; ")
            append("tree-vote agreement on top pick: ${(agreement * 100).format(1)}%; ")
            append("top pick $top at ${(topProb * 100).format(1)}%.")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = probs,
            confidence = confidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    private fun uniform(reason: String): ModelOutput = ModelOutput.fromVector(
        modelName = name, raw = DoubleArray(10) { 0.1 },
        confidence = 0.0, reason = reason, accuracy = 0.0
    )

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
