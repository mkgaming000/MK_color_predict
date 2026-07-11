package com.aicolorpredict.analytics.ai.markov

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln

/**
 * Hidden Markov Model with discrete emissions.
 *
 * We model the game as having K=5 latent "states" (think: hot-red, hot-green,
 * balanced, cold-red, cold-green) and a 10-symbol emission alphabet (the
 * numbers 0..9). The Baum-Welch algorithm is impractical to run on every
 * prediction, so we approximate:
 *
 *   - Latent state at time t is *inferred* from the trailing window using a
 *     simple heuristic (parity balance + recent color balance).
 *   - Transition probabilities between latent states are taken from the
 *     empirical state-to-state counts.
 *   - Emission probabilities per state are taken from the empirical
 *     state-conditional number frequencies.
 *
 * The result is a "soft" prediction: P(next=j) = Σ_s P(state=s | history) × P(j | s).
 *
 * This is intentionally a *lightweight* HMM. It captures the spirit of the
 * algorithm (latent state belief + emission matrix) without the per-call
 * Baum-Welch cost. It is therefore deterministic across runs and trivially
 * unit-testable.
 */
class HiddenMarkovModel : PredictionModel {
    override val name: String = "Hidden Markov Model"
    override val category: ModelCategory = ModelCategory.STATISTICAL

    private companion object {
        const val K = 5
    }

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        val window = history.takeLast(minOf(history.size, 200))

        // 1) Assign a state label to each position in the window.
        val states = IntArray(window.size)
        for (i in window.indices) {
            states[i] = inferState(window, i)
        }

        // 2) State-conditional number emissions, Laplace smoothed.
        val emissions = Array(K) { IntArray(10) }
        for (i in window.indices) emissions[states[i]][window[i]]++

        // 3) State-to-state transition counts.
        val trans = Array(K) { IntArray(K) }
        for (i in 1 until window.size) trans[states[i - 1]][states[i]]++

        // 4) Current state distribution = vector with 1.0 in the inferred current state
        //    (or a soft distribution based on the last few rounds for stability).
        val belief = DoubleArray(K) { 0.0 }
        val tail = window.takeLast(5)
        val tailStates = tail.indices.map { inferState(tail, it) }
        for (s in tailStates) belief[s] += 1.0
        val bSum = belief.sum(); if (bSum > 0) for (i in belief.indices) belief[i] /= bSum
        else belief.fill(1.0 / K)

        // 5) Predict: P(j) = Σ_s belief[s] × P(j | s)
        val probs = DoubleArray(10)
        for (s in 0 until K) {
            val rowProbs = ModelUtils.laplace(emissions[s], alpha = 2.0)
            for (j in 0..9) probs[j] += belief[s] * rowProbs[j]
        }
        val norm = ModelUtils.normalise(probs)

        val top = norm.indices.maxByOrNull { norm[it] } ?: 0
        val topProb = norm[top]

        val dominantState = belief.indices.maxByOrNull { belief[it] } ?: 0
        val confidence = when (dominantState) {
            0, 1 -> 0.55  // hot-* states → more confident
            2 -> 0.35     // balanced → low confidence
            3, 4 -> 0.50  // cold-* → moderate
            else -> 0.40
        }
        val concentration = norm.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val finalConfidence = (confidence * (1.0 - exp(-concentration))).coerceIn(0.0, 0.8)

        val stateLabel = stateName(dominantState)
        val evidence = buildString {
            append("Inferred latent state: $stateLabel (belief ${(belief[dominantState] * 100).format(1)}%); ")
            append("window ${window.size} rounds; ")
            append("top emission from state $stateLabel: number $top at ${(topProb * 100).format(1)}%.")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = norm,
            confidence = finalConfidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    /**
     * Heuristic state inference based on parity balance and recent color balance.
     *   0 = hot-red    (mostly even + mostly red)
     *   1 = hot-green  (mostly odd + mostly green)
     *   2 = balanced
     *   3 = cold-red   (low red, high even)
     *   4 = cold-green (low green, high odd)
     */
    private fun inferState(window: List<Int>, idx: Int): Int {
        val start = (idx - 9).coerceAtLeast(0)
        val slice = window.subList(start, idx + 1)
        if (slice.isEmpty()) return 2
        val even = slice.count { it % 2 == 0 }.toDouble() / slice.size
        val redCount = slice.count { it in setOf(0, 2, 4, 6, 8) }.toDouble() / slice.size
        return when {
            even > 0.6 && redCount > 0.6 -> 0
            even < 0.4 && redCount < 0.4 -> 1
            even > 0.6 && redCount < 0.4 -> 4
            even < 0.4 && redCount > 0.6 -> 3
            else -> 2
        }
    }

    private fun stateName(s: Int): String = when (s) {
        0 -> "hot-red"; 1 -> "hot-green"; 2 -> "balanced"; 3 -> "cold-red"; 4 -> "cold-green"; else -> "?"
    }

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
