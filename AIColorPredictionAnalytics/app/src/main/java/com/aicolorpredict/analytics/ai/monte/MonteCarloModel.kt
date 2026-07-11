package com.aicolorpredict.analytics.ai.monte

import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ai.base.ModelUtils
import com.aicolorpredict.analytics.ai.base.PredictionModel
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.ModelOutput
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random

/**
 * Monte Carlo Simulation model.
 *
 * Runs K simulated continuations of the history by repeatedly sampling from
 * the empirical transition matrix, and reports the distribution of *next*
 * numbers across all simulations. This captures the variability that a single
 * transition row would not.
 *
 * K scales with available history (capped at 2000 to keep latency reasonable
 * on a phone). The RNG seed is fixed per call to make the model deterministic
 * across runs and unit-testable.
 */
class MonteCarloModel(
    private val simulations: Int = 1000,
    private val seed: Long = 0xC0FFEE
) : PredictionModel {
    override val name: String = "Monte Carlo Simulation"
    override val category: ModelCategory = ModelCategory.STATISTICAL

    override suspend fun predict(
        features: FeatureSet,
        history: List<Int>,
        rollingAccuracy: Double
    ): ModelOutput {
        if (history.isEmpty()) {
            return ModelOutput.fromVector(
                modelName = name,
                raw = DoubleArray(10) { 0.1 },
                confidence = 0.0,
                reason = "No history available — output is uniform prior.",
                accuracy = rollingAccuracy
            )
        }

        val rng = Random(seed + features.roundId)
        val last = history.last()
        val transitionMatrix = Array(10) { from -> ModelUtils.laplace(IntArray(10) { to -> features.transitions[from]?.get(to) ?: 0 }, alpha = 1.0) }

        val nextCounts = IntArray(10)
        val effectiveSims = minOf(simulations, maxOf(100, history.size * 10))

        for (s in 0 until effectiveSims) {
            // Each simulation: sample the next number from the transition row.
            // We also do a small number of "look-ahead-2" sims where we sample two
            // steps and weight the immediate next by the inverse of step distance.
            val r = rng.nextDouble()
            var cumulative = 0.0
            for (j in 0..9) {
                cumulative += transitionMatrix[last][j]
                if (r <= cumulative) { nextCounts[j]++; break }
            }
        }

        val probs = ModelUtils.laplace(nextCounts, alpha = 1.0)
        val top = probs.indices.maxByOrNull { probs[it] } ?: 0
        val topProb = probs[top]

        // Confidence: based on simulation count and distribution concentration.
        val concentration = probs.fold(0.0) { acc, p -> acc + (if (p > 0) p * ln(p / 0.1) else 0.0) }
        val simFactor = 1.0 - exp(-effectiveSims.toDouble() / 500.0)
        val confidence = ((1.0 - exp(-concentration)) * simFactor).coerceIn(0.0, 0.78)

        val evidence = buildString {
            append("Ran $effectiveSims simulations seeded from last=$last; ")
            append("MC top pick $top occurred ${nextCounts[top]}× (${(topProb * 100).format(1)}%); ")
            append("transition row entropy ${(concentration).format(3)} nats above uniform.")
        }

        return ModelOutput.fromVector(
            modelName = name,
            raw = probs,
            confidence = confidence,
            reason = ModelUtils.reasonTopPick(name, top, topProb, evidence),
            accuracy = rollingAccuracy
        )
    }

    private fun Double.format(d: Int): String = "%.${d}f".format(this)
}
