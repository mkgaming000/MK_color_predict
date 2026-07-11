package com.aicolorpredict.analytics.feature

import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.colorsForNumber

/**
 * Builds and queries the first-order transition matrix M[i][j] = P(next = j | prev = i).
 *
 * - Add observations in chronological order; the matrix updates incrementally.
 * - [predict] returns a 10-element probability vector with Laplace smoothing.
 * - [transitionStats] returns the [TransitionStats] domain object for a given
 *   "from" number — used by the Transition Analysis screen.
 *
 * The matrix is intentionally kept as an IntArray of size 100 (10x10) for cache
 * friendliness and trivial JSON serialization.
 */
class TransitionMatrix {

    /** counts[from * 10 + to] */
    private val counts = IntArray(100)
    /** rowSums[from] = total transitions leaving `from` */
    private val rowSums = IntArray(10)

    fun observe(prev: Int, next: Int) {
        require(prev in 0..9 && next in 0..9)
        counts[prev * 10 + next]++
        rowSums[prev]++
    }

    fun row(from: Int): DoubleArray {
        require(from in 0..9)
        val total = rowSums[from]
        val out = DoubleArray(10)
        if (total == 0) { out.fill(1.0 / 10.0); return out }
        // Laplace smoothing with alpha=1, denom = total + 10
        val denom = total + 10
        for (j in 0..9) {
            out[j] = (counts[from * 10 + j] + 1.0) / denom
        }
        return out
    }

    fun totalTransitions(from: Int): Int = rowSums[from]

    /** Full 10x10 transition matrix as a fresh DoubleArray of length 100. */
    fun fullMatrix(): DoubleArray {
        val out = DoubleArray(100)
        for (i in 0..9) {
            val row = row(i)
            for (j in 0..9) out[i * 10 + j] = row[j]
        }
        return out
    }

    fun countsMatrix(): IntArray = counts.copyOf()

    /**
     * Computes the "average gap" statistic for each target number `j`, given a
     * specific `from` number. Gap is defined as the number of transitions observed
     * between two consecutive `from -> j` events, averaged over all such events.
     */
    fun averageGaps(from: Int): DoubleArray {
        val out = DoubleArray(10) { Double.NaN }
        // We cannot recover inter-arrival spacing from counts alone; this requires
        // the original sequence. The feature engineer passes that in via
        // [computeAverageGapsFromSequence]. Here we just return NaN as a sentinel.
        return out
    }
}

/** Stateless helpers that need the full history list. */
object TransitionAnalytics {

    /**
     * For a given `from` number, scan the history and produce:
     *   - the smoothed next-number probability vector
     *   - the next-color probability map
     *   - the average inter-arrival gap per target
     */
    fun build(from: Int, history: List<Int>): com.aicolorpredict.analytics.domain.model.TransitionStats {
        require(from in 0..9)
        val nextCounts = IntArray(10)
        var total = 0
        val gapLastSeen = IntArray(10) { -1 }
        val gapSums = DoubleArray(10)
        val gapCounts = IntArray(10)

        for (i in 1 until history.size) {
            if (history[i - 1] != from) continue
            val next = history[i]
            nextCounts[next]++
            total++
            // Gap tracking: distance from the previous `from -> next` event.
            if (gapLastSeen[next] >= 0) {
                val gap = i - gapLastSeen[next]
                gapSums[next] = gapSums[next] + gap.toDouble()
                gapCounts[next]++
            }
            gapLastSeen[next] = i
        }

        val denom = total + 10
        val nextProb = DoubleArray(10) { (nextCounts[it] + 1.0) / denom }

        var pRed = 0.0; var pGreen = 0.0; var pViolet = 0.0
        for (j in 0..9) {
            val p = nextProb[j]
            val cols = colorsForNumber(j)
            if (cols.contains(BallColor.RED)) pRed += p
            if (cols.contains(BallColor.GREEN)) pGreen += p
            if (cols.contains(BallColor.VIOLET)) pViolet += p
        }
        val rg = pRed + pGreen; if (rg > 0) { pRed /= rg; pGreen /= rg }

        val avgGaps = (0..9).associateWith { j ->
            if (gapCounts[j] == 0) Double.NaN else gapSums[j] / gapCounts[j]
        }

        return com.aicolorpredict.analytics.domain.model.TransitionStats(
            fromNumber = from,
            totalTransitions = total,
            nextNumberCounts = (0..9).associateWith { nextCounts[it] },
            nextNumberProbabilities = (0..9).associateWith { nextProb[it] },
            nextColorCounts = mapOf(
                BallColor.RED to nextCounts.indices.sumOf { if (colorsForNumber(it).contains(BallColor.RED)) nextCounts[it] else 0 },
                BallColor.GREEN to nextCounts.indices.sumOf { if (colorsForNumber(it).contains(BallColor.GREEN)) nextCounts[it] else 0 },
                BallColor.VIOLET to nextCounts.indices.sumOf { if (colorsForNumber(it).contains(BallColor.VIOLET)) nextCounts[it] else 0 }
            ),
            nextColorProbabilities = mapOf(
                BallColor.RED to pRed, BallColor.GREEN to pGreen, BallColor.VIOLET to pViolet
            ),
            averageGap = avgGaps,
            historicalCount = total
        )
    }
}
