package com.aicolorpredict.analytics.ai.bayesian

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln

/**
 * Naive-Bayes-style Bayesian Network.
 *
 * We treat the next number as a class label and the following binary features as
 * conditionally independent given the class:
 *
 *   - parity of the previous number (odd/even)
 *   - color of the previous number (red/green primary)
 *   - whether the previous number carried violet
 *   - hot/cold state of the previous number (top-3 hot = 1)
 *   - recent color balance (more green than red in last 10)
 *
 * Posterior ∝ Likelihood × Prior. We use Laplace smoothing on every conditional
 * probability table. This is the textbook Naive Bayes classifier — fast,
 * explainable, and a useful sanity-check baseline against the more complex models.
 */
class BayesianNetworkModel : PredictionModel {
    override val name: String = "Bayesian Network"
    override val category: ModelCategory = ModelCategory.STATISTICAL

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        val window = history.takeLast(minOf(history.size, 500))

        // Build conditional probability tables (CPTs):
        // For each class j in 0..9 and each feature f, count P(f | j).
        val priorCounts = IntArray(10)
        // Feature 0: prevParity (0=even, 1=odd)
        // Feature 1: prevColor (0=red, 1=green)
        // Feature 2: prevViolet (0/1)
        // Feature 3: prevHot (0/1)
        // Feature 4: recentGreenHeavy (0/1)
        val cpt = Array(5) { Array(10) { IntArray(2) } }

        for (i in 1 until window.size) {
            val cls = window[i]
            val prev = window[i - 1]
            priorCounts[cls]++

            val prevParity = if (prev % 2 == 0) 0 else 1
            val prevColor = if (prev % 2 == 0) 0 else 1  // 0,2,4,6,8 = red-primary
            val prevViolet = if (prev == 0 || prev == 5) 1 else 0
            val prevHot = if (features.hotNumbers.contains(prev)) 1 else 0
            val recentGreen = window.subList((i - 10).coerceAtLeast(0), i)
                .count { it in setOf(1, 3, 5, 7, 9) } > 5
            val recentGreenHeavy = if (recentGreen) 1 else 0

            val feats = intArrayOf(prevParity, prevColor, prevViolet, prevHot, recentGreenHeavy)
            for (f in feats.indices) cpt[f][cls][feats[f]]++
        }

        // Compute prior.
        val prior = ModelUtils.laplace(priorCounts, alpha = 1.0)

        // Compute the feature vector for the current "now" state.
        val last = window.lastOrNull() ?: 0
        val lastParity = if (last % 2 == 0) 0 else 1
        val lastColor = if (last % 2 == 0) 0 else 1
        val lastViolet = if (last == 0 || last == 5) 1 else 0
        val lastHot = if (features.hotNumbers.contains(last)) 1 else 0
        val recentGreenHeavy = window.takeLast(10).count { it in setOf(1, 3, 5, 7, 9) } > 5
        val lastRecentGreenHeavy = if (recentGreenHeavy) 1 else 0
        val observed = intArrayOf(lastParity, lastColor, lastViolet, lastHot, lastRecentGreenHeavy)

        // Posterior ∝ prior × Π P(f_observed | j)
        val posterior = DoubleArray(10)
        for (j in 0..9) {
            var p = prior[j]
            for (f in observed.indices) {
                val row = cpt[f][j]
                val total = row[0] + row[1] + 2  // Laplace alpha=1 per binary outcome
                val prob = (row[observed[f]] + 1.0) / total
                p *= prob
            }
            posterior[j] = p
        }
        val norm = ModelUtils.normalise(posterior)

        val top = norm.indices.maxByOrNull { norm[it] } ?: 0
        val topProb = norm[top]

        val concentration = norm.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val sampleFactor = 1.0 - exp(-window.size.toDouble() / 200.0)
        val confidence = ((1.0 - exp(-concentration)) * sampleFactor).coerceIn(0.0, 0.80)

        val evidence = buildString {
            append("Prior favours ${prior.indices.maxByOrNull { prior[it] } ?: 0}; ")
            append("observed features: parity=$lastParity, color=$lastColor, violet=$lastViolet, hot=$lastHot, greenHeavy=$lastRecentGreenHeavy; ")
            append("posterior top = $top at ${(topProb * 100).format(1)}%.")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = norm,
            confidence = confidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
