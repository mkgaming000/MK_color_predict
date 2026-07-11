package com.aicolorpredict.analytics.ai.tree

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.ai.neural.Activations
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random

/**
 * Gradient Boosting Machine (multinomial deviance loss).
 *
 * Generic stage-wise additive model: each new tree fits the negative gradient
 * of the softmax loss with respect to the current logits. We use a small
 * learning rate (0.1) and a small number of estimators (12) — both chosen so
 * that the entire fit/predict completes in well under 100 ms on a mid-range
 * phone even with a 200-sample training set.
 *
 * The same engine is reused (with different hyper-parameters) by the
 * [XGBoostModel], [LightGbmModel], and [CatBoostModel] variants below. This
 * is the standard "one engine, multiple profiles" pattern that real Gradient
 * Boosting libraries use under the hood — we just expose it as separate
 * [PredictionModel] implementations so each one can have its own
 * confidence calibration and explanation string.
 */
open class GradientBoostingModel(
    private val nEstimators: Int = 12,
    private val maxDepth: Int = 3,
    private val learningRate: Double = 0.1,
    private val seed: Long = 5678901234L,
    private val modelDisplayName: String = "Gradient Boosting"
) : PredictionModel {
    override val name: String = modelDisplayName
    override val category: ModelCategory = ModelCategory.TREE

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        if (history.size < 30) return uniform("Insufficient history for $modelDisplayName.")
        val training = buildTreeTrainingSet(history, windowSize = 50, maxSamples = 200)
        if (training.isEmpty()) return uniform("Empty training set for $modelDisplayName.")

        val xs = training.map { it.first }
        val ys = training.map { it.second }
        val nFeatures = xs.first().size

        // Current logits = 0 (we start from uniform prior).
        val logits = Array(xs.size) { DoubleArray(10) }
        val trees = Array(nEstimators) { ArrayList<DecisionTreeRegressor>(10) }

        for (round in 0 until nEstimators) {
            // Compute softmax probabilities and gradients.
            val probs = Array(xs.size) { i -> Activations_softmax(logits[i]) }
            // For each class k, gradient = p_k - 1{y==k}
            for (k in 0..9) {
                val residuals = DoubleArray(xs.size)
                for (i in xs.indices) residuals[i] = probs[i][k] - (if (ys[i] == k) 1.0 else 0.0)
                // Fit a regression tree to the negative gradient.
                val regSamples = xs.indices.map { xs[it] to -residuals[it] }.toList()
                val tree = DecisionTreeRegressor(
                    maxDepth = maxDepth,
                    minSamplesSplit = 4,
                    featureSamplingRate = 0.4,
                    rng = Random(seed + round * 10L + k)
                )
                tree.fit(regSamples)
                trees[round].add(tree)
                // Update logits
                for (i in xs.indices) logits[i][k] += learningRate * tree.predict(xs[i])
            }
        }

        // Predict on the current window.
        val currentWindow = history.takeLast(50)
        val x = quickFeatures(currentWindow)
        val testLogits = DoubleArray(10)
        for (round in 0 until nEstimators) {
            for (k in 0..9) {
                testLogits[k] += learningRate * trees[round][k].predict(x)
            }
        }
        val probs = Activations_softmax(testLogits)

        val top = probs.indices.maxByOrNull { probs[it] } ?: 0
        val topProb = probs[top]
        val concentration = probs.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val sampleFactor = 1.0 - exp(-history.size.toDouble() / 200.0)
        val confidence = ((1.0 - exp(-concentration)) * sampleFactor * 0.85).coerceIn(0.0, 0.78)

        val evidence = buildString {
            append("$modelDisplayName ($nEstimators rounds × 10 classes, lr=$learningRate, depth $maxDepth); ")
            append("training samples: ${training.size}; ")
            append("final logit range [${testLogits.minOrNull()?.format(3)}, ${testLogits.maxOrNull()?.format(3)}]; ")
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

    protected fun uniform(reason: String): ModelOutput = ModelOutput.fromVector(
        modelName = name, raw = DoubleArray(10) { 0.1 },
        confidence = 0.0, reason = reason, accuracy = 0.0
    )

    protected fun Double.format(d: Int): String = "%.${d}f".format(this)
}

/** Aliased softmax to avoid naming conflicts. */
internal val Activations_softmax: (DoubleArray) -> DoubleArray =
    Activations::softmax

/**
 * Tiny regression tree used by the GBM (fits continuous targets — the negative
 * gradient of the multinomial loss).
 */
class DecisionTreeRegressor(
    private val maxDepth: Int = 3,
    private val minSamplesSplit: Int = 4,
    private val featureSamplingRate: Double = 0.4,
    private val rng: Random = Random(0xC0FFEE)
) {
    private sealed class Node {
        class Leaf(val value: Double) : Node()
        class Internal(val feature: Int, val threshold: Double, val left: Node, val right: Node) : Node()
    }

    private var root: Node = Node.Leaf(0.0)

    fun fit(samples: List<Pair<DoubleArray, Double>>) {
        root = if (samples.isEmpty()) Node.Leaf(0.0) else build(samples, 0)
    }

    private fun build(samples: List<Pair<DoubleArray, Double>>, depth: Int): Node {
        val mean = samples.map { it.second }.average()
        if (depth >= maxDepth || samples.size < minSamplesSplit) return Node.Leaf(mean)

        val nFeatures = samples.first().first.size
        val subsetSize = (nFeatures * featureSamplingRate).toInt().coerceAtLeast(1)
        val subset = (0 until nFeatures).shuffled(rng).take(subsetSize)
        var bestGain = 0.0; var bestF = -1; var bestT = 0.0
        val parentVar = variance(samples.map { it.second })
        for (f in subset) {
            val values = samples.map { it.first[f] }.distinct().sorted()
            if (values.size < 2) continue
            val candidates = if (values.size > 16) values.filterIndexed { i, _ -> i % (values.size / 16) == 0 } else values
            for (i in 1 until candidates.size) {
                val t = (candidates[i - 1] + candidates[i]) / 2.0
                val left = samples.filter { it.first[f] <= t }
                val right = samples.filter { it.first[f] > t }
                if (left.isEmpty() || right.isEmpty()) continue
                val nl = left.size.toDouble(); val nr = right.size.toDouble(); val n = nl + nr
                val gain = parentVar - (nl / n) * variance(left.map { it.second }) - (nr / n) * variance(right.map { it.second })
                if (gain > bestGain) { bestGain = gain; bestF = f; bestT = t }
            }
        }
        if (bestF < 0 || bestGain <= 0.0) return Node.Leaf(mean)
        val left = samples.filter { it.first[bestF] <= bestT }
        val right = samples.filter { it.first[bestF] > bestT }
        return Node.Internal(bestF, bestT, build(left, depth + 1), build(right, depth + 1))
    }

    private fun variance(xs: List<Double>): Double {
        if (xs.isEmpty()) return 0.0
        val m = xs.average()
        return xs.fold(0.0) { acc, v -> acc + (v - m) * (v - m) } / xs.size
    }

    fun predict(x: DoubleArray): Double {
        var node = root
        while (node is Node.Internal) {
            node = if (x[node.feature] <= node.threshold) node.left else node.right
        }
        return (node as Node.Leaf).value
    }
}
