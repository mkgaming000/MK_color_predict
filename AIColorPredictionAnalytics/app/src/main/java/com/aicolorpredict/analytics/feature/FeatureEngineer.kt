package com.aicolorpredict.analytics.feature

import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.Cycle
import com.aicolorpredict.analytics.domain.model.FeatureSet
import com.aicolorpredict.analytics.domain.model.Run
import com.aicolorpredict.analytics.domain.model.colorsForNumber

/**
 * Turns a raw list of past numbers into a fully-formed [FeatureSet].
 *
 * The output is the single shared "feature payload" of the system — every AI
 * model consumes it. Keeping the computation in one place guarantees that
 * every model sees the *same* view of the history and cannot silently
 * disagree about basic facts.
 *
 * Performance: the entire pipeline is O(n) for n history entries with a small
 * constant — it comfortably handles 10,000+ rounds on a mid-range phone.
 */
class FeatureEngineer {

    fun build(roundId: Long, history: List<Int>): FeatureSet {
        val n = history.size
        val recent = history.takeLast(minOf(n, 1000))

        // Number frequency
        val numberFreq = IntArray(10)
        for (v in recent) numberFreq[v]++
        val numberFrequency = (0..9).associateWith { numberFreq[it] }

        // Color frequency
        var pRed = 0; var pGreen = 0; var pViolet = 0
        for (v in recent) {
            val cols = colorsForNumber(v)
            if (cols.contains(BallColor.RED)) pRed++
            if (cols.contains(BallColor.GREEN)) pGreen++
            if (cols.contains(BallColor.VIOLET)) pViolet++
        }
        val colorFrequency = mapOf(
            BallColor.RED to pRed, BallColor.GREEN to pGreen, BallColor.VIOLET to pViolet
        )

        // Transitions
        val matrix = TransitionMatrix()
        for (i in 1 until recent.size) matrix.observe(recent[i - 1], recent[i])
        val transitions: Map<Int, Map<Int, Int>> = (0..9).associateWith { from ->
            val counts = matrix.countsMatrix()
            (0..9).associateWith { to -> counts[from * 10 + to] }
        }
        val transitionCounts = (0..9).associateWith { matrix.totalTransitions(it) }

        // Gaps: rounds since last occurrence of each number (0 = the number was the most recent)
        val gaps = IntArray(10) { n }
        val lastSeen = IntArray(10) { -1 }
        for (i in recent.indices) {
            val v = recent[i]
            if (lastSeen[v] >= 0) {
                val gap = recent.size - 1 - i
                if (gap < gaps[v]) gaps[v] = gap
            }
            lastSeen[v] = i
        }
        // If we have not seen a number at all, gap = n (history length) — caller can detect that.
        val gapMap = (0..9).associateWith { gaps[it] }

        // Hot / cold
        val sortedByFreq = (0..9).sortedByDescending { numberFreq[it] }
        val hotNumbers = sortedByFreq.take(3).toList()
        val coldNumbers = sortedByFreq.takeLast(3).reversed().toList()

        // Momentum (linearly weighted frequency over short vs long windows)
        val recentMomentum = momentum(recent, 30)
        val longTermMomentum = momentum(recent, minOf(n, 200))

        // Parity & size ratios
        val total = recent.size.coerceAtLeast(1)
        val oddCount = recent.count { it % 2 == 1 }
        val smallCount = recent.count { it in 0..4 }
        val oddRatio = oddCount.toDouble() / total
        val evenRatio = 1.0 - oddRatio
        val smallRatio = smallCount.toDouble() / total
        val bigRatio = 1.0 - smallRatio
        val greenRatio = pGreen.toDouble() / total
        val redRatio = pRed.toDouble() / total
        val violetRatio = pViolet.toDouble() / total

        // Rolling averages (probability of each number in the trailing window)
        val rollingAverages = (0..9).associateWith { numberFreq[it].toDouble() / total }

        // Entropy & variance
        val probs = DoubleArray(10) { numberFreq[it].toDouble() / total }
        val entropy = StatsCalculator.entropy(probs)
        val variance = StatsCalculator.variance(recent.map { it.toDouble() }.toDoubleArray())

        // Patterns
        val patternFrequency: Map<String, Int> = run {
            val counts = mutableMapOf<String, Int>()
            PatternDetector.repeatingSequences(recent).forEach { (seq, c) ->
                counts[seq.joinToString(",")] = c
            }
            PatternDetector.mirrorPatterns(recent).forEach { (L, c) ->
                counts["mirror_L$L"] = c
            }
            counts
        }
        val cycles: List<Cycle> = PatternDetector.cycles(recent)
        val runs: List<Run> = PatternDetector.alternatingColorRuns(recent)

        // Time intervals — we don't have real timestamps here; use position index deltas
        val timeIntervals = if (recent.size > 1) (1 until recent.size).map { 1L }.toList() else emptyList()

        // Historical similarity: cosine similarity between the recent 20-window and
        // every other 20-window in history. Higher = more similar.
        val historicalSimilarity: Map<Int, Double> = run {
            val W = 20
            if (recent.size < 2 * W) return@run (0..9).associateWith { 0.0 }
            val tail = recent.takeLast(W).map { it.toDouble() }.toDoubleArray()
            val out = mutableMapOf<Int, Double>()
            val sims = DoubleArray(10) { 0.0 }
            var count = 0
            for (i in 0..recent.size - 2 * W) {
                val window = recent.subList(i, i + W).map { it.toDouble() }.toDoubleArray()
                val s = StatsCalculator.cosine(tail, window)
                // For each number in the position *after* this similar window, sum up similarity.
                val next = recent[i + W]
                sims[next] += s
                count++
            }
            // Normalise to a probability-like value.
            val total2 = sims.sum().coerceAtLeast(1e-9)
            for (j in 0..9) out[j] = sims[j] / total2
            out
        }

        // Feature vector — flattened representation consumed by tree / neural models.
        val featureVector = buildFeatureVector(
            numberFrequency, gapMap, transitions, recentMomentum, longTermMomentum,
            oddRatio, evenRatio, smallRatio, bigRatio, greenRatio, redRatio, violetRatio,
            rollingAverages, entropy, variance, historicalSimilarity
        )

        return FeatureSet(
            roundId = roundId,
            totalSamples = n,
            recent = recent,
            numberFrequency = numberFrequency,
            colorFrequency = colorFrequency,
            transitions = transitions,
            transitionCounts = transitionCounts,
            gaps = gapMap,
            hotNumbers = hotNumbers,
            coldNumbers = coldNumbers,
            recentMomentum = recentMomentum,
            longTermMomentum = longTermMomentum,
            oddRatio = oddRatio,
            evenRatio = evenRatio,
            smallRatio = smallRatio,
            bigRatio = bigRatio,
            greenRatio = greenRatio,
            redRatio = redRatio,
            violetRatio = violetRatio,
            rollingAverages = rollingAverages,
            entropy = entropy,
            variance = variance,
            patternFrequency = patternFrequency,
            cycles = cycles,
            runs = runs,
            timeIntervals = timeIntervals,
            historicalSimilarity = historicalSimilarity,
            featureVector = featureVector
        )
    }

    /** Linearly-weighted probability of each number over the trailing `window` rounds. */
    private fun momentum(history: List<Int>, window: Int): Map<Int, Double> {
        val tail = history.takeLast(minOf(history.size, window))
        if (tail.isEmpty()) return (0..9).associateWith { 0.1 }
        val weights = DoubleArray(tail.size) { (it + 1).toDouble() }
        val total = weights.sum()
        val out = DoubleArray(10) { 0.0 }
        for (i in tail.indices) out[tail[i]] += weights[i]
        for (j in 0..9) out[j] /= total
        return (0..9).associateWith { out[it] }
    }

    /**
     * Builds the dense feature vector consumed by tree / neural models.
     * Order is fixed and documented so saved models can deserialise consistently.
     */
    private fun buildFeatureVector(
        numberFrequency: Map<Int, Int>,
        gaps: Map<Int, Int>,
        transitions: Map<Int, Map<Int, Int>>,
        recentMomentum: Map<Int, Double>,
        longTermMomentum: Map<Int, Double>,
        oddRatio: Double, evenRatio: Double,
        smallRatio: Double, bigRatio: Double,
        greenRatio: Double, redRatio: Double, violetRatio: Double,
        rollingAverages: Map<Int, Double>,
        entropy: Double, variance: Double,
        historicalSimilarity: Map<Int, Double>
    ): DoubleArray {
        val out = ArrayList<Double>(256)
        val total = numberFrequency.values.sumOf { it.toLong() }.toDouble().coerceAtLeast(1.0)
        // 10 normalized frequencies
        for (n in 0..9) out += numberFrequency[n]!!.toDouble() / total
        // 10 gaps (normalised by total)
        for (n in 0..9) out += gaps[n]!!.toDouble() / total
        // 100 transition probabilities
        for (i in 0..9) {
            val rowTotal = transitions[i]!!.values.sumOf { it.toLong() }.toDouble().coerceAtLeast(1.0)
            for (j in 0..9) out += transitions[i]!![j]!!.toDouble() / rowTotal
        }
        // 10 recent momentum
        for (n in 0..9) out += recentMomentum[n]!!
        // 10 long-term momentum
        for (n in 0..9) out += longTermMomentum[n]!!
        // 7 scalar ratios
        out += oddRatio; out += evenRatio; out += smallRatio; out += bigRatio
        out += greenRatio; out += redRatio; out += violetRatio
        // 10 rolling averages
        for (n in 0..9) out += rollingAverages[n]!!
        // 10 historical similarity
        for (n in 0..9) out += historicalSimilarity[n]!!
        // entropy, variance
        out += entropy; out += variance
        return out.toDoubleArray()
    }
}
