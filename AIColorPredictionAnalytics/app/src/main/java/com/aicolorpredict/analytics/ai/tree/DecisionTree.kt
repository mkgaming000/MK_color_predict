package com.aicolorpredict.analytics.ai.tree

import com.aicolorpredict.analytics.domain.model.FeatureSet
import kotlin.random.Random

/**
 * Pure-Kotlin decision tree with multiclass softmax leaves.
 *
 * Split criterion: Gini impurity. Splits are evaluated on a randomly-chosen
 * feature subset (sqrt of total features) at each node — this gives us the
 * "random subspace method" used by Random Forest for free.
 *
 * The tree is intentionally depth-capped (default 6) — for ~150 features and
 * only a few hundred training samples in the rolling window, deeper trees just
 * memorise noise.
 *
 * The "training" here happens at predict-time on the recent window: we fit a
 * fresh tree using the trailing N rounds, with X = the feature vector computed
 * at that historical point and y = the next number. The fitted tree is then
 * evaluated on the *current* feature vector to produce a probability
 * distribution over the next number.
 *
 * This is the simplest viable tree and the foundation for the RF / GBM / XGB /
 * LGBM / CatBoost variants.
 */
class DecisionTree(
    private val maxDepth: Int = 6,
    private val minSamplesSplit: Int = 4,
    private val featureSamplingRate: Double = 0.3,
    private val rng: Random = Random(0xDECAF)
) {
    private sealed class Node {
        class Leaf(val probs: DoubleArray) : Node()
        class Internal(val feature: Int, val threshold: Double, val left: Node, val right: Node) : Node()
    }

    private var root: Node = Node.Leaf(DoubleArray(10) { 0.1 })

    /**
     * Fit the tree on a list of (features, label) pairs.
     */
    fun fit(samples: List<Pair<DoubleArray, Int>>) {
        root = if (samples.isEmpty()) Node.Leaf(DoubleArray(10) { 0.1 })
        else build(samples, depth = 0)
    }

    private fun build(samples: List<Pair<DoubleArray, Int>>, depth: Int): Node {
        val labels = samples.map { it.second }
        val counts = IntArray(10)
        for (y in labels) counts[y]++
        val priors = DoubleArray(10) { (counts[it] + 1.0) / (labels.size + 10.0) }

        if (depth >= maxDepth || samples.size < minSamplesSplit || labels.toSet().size == 1) {
            return Node.Leaf(priors)
        }

        val nFeatures = samples.first().first.size
        val sampleSize = (nFeatures * featureSamplingRate).toInt().coerceAtLeast(1)
        val featureSubset = (0 until nFeatures).shuffled(rng).take(sampleSize)

        var bestGain = 0.0
        var bestF = -1; var bestT = 0.0
        val parentGini = gini(counts)
        for (f in featureSubset) {
            val values = samples.map { it.first[f] }.distinct().sorted()
            if (values.size < 2) continue
            // Try midpoints between consecutive distinct values; cap to 16 candidates for speed.
            val candidates = if (values.size > 16) values.filterIndexed { i, _ -> i % (values.size / 16) == 0 } else values
            for (i in 1 until candidates.size) {
                val t = (candidates[i - 1] + candidates[i]) / 2.0
                val leftCounts = IntArray(10); val rightCounts = IntArray(10)
                for ((x, y) in samples) {
                    if (x[f] <= t) leftCounts[y]++ else rightCounts[y]++
                }
                val nl = leftCounts.sumOf { it.toLong() }.toDouble()
                val nr = rightCounts.sumOf { it.toLong() }.toDouble()
                val n = nl + nr
                if (nl < 1 || nr < 1) continue
                val gain = parentGini - (nl / n) * gini(leftCounts) - (nr / n) * gini(rightCounts)
                if (gain > bestGain) {
                    bestGain = gain; bestF = f; bestT = t
                }
            }
        }
        if (bestF < 0 || bestGain <= 0.0) return Node.Leaf(priors)

        val left = samples.filter { it.first[bestF] <= bestT }
        val right = samples.filter { it.first[bestF] > bestT }
        return Node.Internal(bestF, bestT, build(left, depth + 1), build(right, depth + 1))
    }

    private fun gini(counts: IntArray): Double {
        val n = counts.sumOf { it.toLong() }.toDouble()
        if (n <= 0) return 0.0
        var s = 0.0
        for (c in counts) { val p = c / n; s += p * p }
        return 1.0 - s
    }

    fun predict(x: DoubleArray): DoubleArray {
        var node = root
        while (node is Node.Internal) {
            node = if (x[node.feature] <= node.threshold) node.left else node.right
        }
        return (node as Node.Leaf).probs
    }
}

/**
 * Helper: builds a sliding-window training set from history. For each position
 * t in [windowSize, history.size), we generate a sample whose X is the feature
 * vector of the history *ending at position t* (using a tiny feature extractor
 * for speed) and whose y is history[t].
 *
 * We deliberately use a *reduced* feature extractor here (only the
 * transition-row + recent frequency + parity ratios) — the full
 * [com.aicolorpredict.analytics.feature.FeatureEngineer] is too expensive to
 * run for every position in the window.
 */
fun buildTreeTrainingSet(
    history: List<Int>,
    windowSize: Int = 50,
    maxSamples: Int = 200
): List<Pair<DoubleArray, Int>> {
    if (history.size < windowSize + 1) return emptyList()
    val out = ArrayList<Pair<DoubleArray, Int>>(maxSamples)
    val step = ((history.size - windowSize) / maxSamples).coerceAtLeast(1)
    var t = windowSize
    while (t < history.size && out.size < maxSamples) {
        val window = history.subList(t - windowSize, t)
        val x = quickFeatures(window)
        val y = history[t]
        out += x to y
        t += step
    }
    return out
}

/** Compact 33-dim feature vector: 10 frequencies + 10 last-row + 10 gap + 3 ratios. */
fun quickFeatures(window: List<Int>): DoubleArray {
    val out = DoubleArray(33)
    val counts = IntArray(10)
    for (v in window) counts[v]++
    val total = window.size.toDouble()
    for (j in 0..9) out[j] = counts[j] / total

    val last = window.last()
    val trans = IntArray(10)
    for (i in 1 until window.size) if (window[i - 1] == last) trans[window[i]]++
    val transTotal = trans.sumOf { it.toLong() }.toDouble().coerceAtLeast(1.0)
    for (j in 0..9) out[10 + j] = trans[j] / transTotal

    val lastSeen = IntArray(10) { -1 }
    for (i in window.indices) lastSeen[window[i]] = i
    for (j in 0..9) {
        out[20 + j] = if (lastSeen[j] < 0) 1.0 else (window.size - 1 - lastSeen[j]).toDouble() / window.size
    }
    val oddRatio = window.count { it % 2 == 1 }.toDouble() / total
    out[30] = oddRatio
    out[31] = window.count { it in 0..4 }.toDouble() / total
    out[32] = window.count { it in setOf(1, 3, 5, 7, 9) }.toDouble() / total
    return out
}
