package com.aicolorpredict.analytics.ai.learning

import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.colorsForNumber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln

/**
 * In-memory incremental statistics cache.
 *
 * Instead of recomputing frequency tables, transition matrices, and color
 * counts from scratch on every prediction (O(n) per call), this cache
 * maintains running aggregates that update in O(1) per new round.
 *
 * The cache is **process-lifetime** — it is rebuilt from the DB on first
 * access after app start (via [rebuildFrom]). Subsequent calls to [update]
 * are O(1). This gives us:
 *
 *   - First prediction after cold start: O(n) rebuild (acceptable, one-time)
 *   - Every subsequent prediction: O(1) statistics + O(k) model inference
 *   - Scales linearly to 1,000,000+ records with no per-call degradation
 *
 * Thread-safe via a single `synchronized` lock — the critical sections are
 * tiny (a few IntArray increments) so contention is negligible.
 */
@Singleton
class IncrementalStatsCache @Inject constructor() {

    @Volatile private var initialized = false

    // Running counts — all O(1) to update.
    private val numberCounts = IntArray(10)
    private val colorCounts = IntArray(3) // RED, GREEN, VIOLET
    private val transitionCounts = IntArray(100) // [from*10 + to]
    private val transitionRowSums = IntArray(10)
    private var totalRounds = 0
    private var lastNumber = -1

    // For rolling/sliding-window analysis — keeps the last `windowSize` numbers.
    private val window = ArrayDeque<Int>()
    private val windowSize = 1000

    // For gap tracking: last seen index per number.
    private val lastSeenIndex = IntArray(10) { -1 }
    private var currentIndex = 0

    /** Rebuild the cache from a full history. Call once on cold start. */
    synchronized fun rebuildFrom(history: List<Int>) {
        // Reset
        java.util.Arrays.fill(numberCounts, 0)
        java.util.Arrays.fill(colorCounts, 0)
        java.util.Arrays.fill(transitionCounts, 0)
        java.util.Arrays.fill(transitionRowSums, 0)
        java.util.Arrays.fill(lastSeenIndex, -1)
        window.clear()
        totalRounds = 0
        lastNumber = -1
        currentIndex = 0

        for (n in history) {
            applyRound(n)
        }
        initialized = true
    }

    /** Add a single new round in O(1). */
    synchronized fun update(number: Int) {
        if (!initialized) return
        applyRound(number)
    }

    private fun applyRound(number: Int) {
        require(number in 0..9)

        // Frequency
        numberCounts[number]++
        totalRounds++

        // Color counts
        val colors = colorsForNumber(number)
        if (colors.contains(BallColor.RED)) colorCounts[0]++
        if (colors.contains(BallColor.GREEN)) colorCounts[1]++
        if (colors.contains(BallColor.VIOLET)) colorCounts[2]++

        // Transition: last -> number
        if (lastNumber >= 0) {
            transitionCounts[lastNumber * 10 + number]++
            transitionRowSums[lastNumber]++
        }
        lastNumber = number

        // Gap tracking
        lastSeenIndex[number] = currentIndex
        currentIndex++

        // Sliding window
        window.addLast(number)
        while (window.size > windowSize) window.removeFirst()
    }

    /** O(1) frequency probability (Laplace-smoothed). */
    fun numberFrequency(normalised: Boolean = true): DoubleArray {
        val total = totalRounds + 10.0
        return DoubleArray(10) { (numberCounts[it] + 1.0) / total }
    }

    /** O(1) color probability. */
    fun colorFrequency(): Map<BallColor, Double> {
        val total = totalRounds.coerceAtLeast(1).toDouble()
        return mapOf(
            BallColor.RED to colorCounts[0] / total,
            BallColor.GREEN to colorCounts[1] / total,
            BallColor.VIOLET to colorCounts[2] / total
        )
    }

    /** O(1) transition row for a given `from` number (Laplace-smoothed). */
    fun transitionRow(from: Int): DoubleArray {
        val rowSum = transitionRowSums[from]
        val denom = rowSum + 10.0
        return DoubleArray(10) { (transitionCounts[from * 10 + it] + 1.0) / denom }
    }

    /** O(1) gap (rounds since last occurrence) for each number. */
    fun gaps(): IntArray {
        val out = IntArray(10)
        for (n in 0..9) {
            out[n] = if (lastSeenIndex[n] < 0) totalRounds else currentIndex - 1 - lastSeenIndex[n]
        }
        return out
    }

    /** Hot numbers (top-k by frequency) — O(10 log 10) ≈ O(1). */
    fun hotNumbers(k: Int = 3): List<Int> =
        (0..9).sortedByDescending { numberCounts[it] }.take(k)

    /** Cold numbers (bottom-k by frequency) — O(10 log 10) ≈ O(1). */
    fun coldNumbers(k: Int = 3): List<Int> =
        (0..9).sortedBy { numberCounts[it] }.take(k)

    /** Sliding-window tail (most recent `n` numbers, most-recent last). */
    fun recentWindow(n: Int): List<Int> {
        val take = minOf(n, window.size)
        return window.toList().takeLast(take)
    }

    /** Shannon entropy of the current frequency distribution (nats). */
    fun entropy(): Double {
        val total = totalRounds.coerceAtLeast(1).toDouble()
        var h = 0.0
        for (c in numberCounts) {
            if (c > 0) {
                val p = c / total
                h -= p * ln(p)
            }
        }
        return h
    }

    fun totalRounds(): Int = totalRounds
    fun isInitialized(): Boolean = initialized
}
