package com.aicolorpredict.analytics.feature

import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.Cycle
import com.aicolorpredict.analytics.domain.model.Run
import com.aicolorpredict.analytics.domain.model.colorsForNumber
import com.aicolorpredict.analytics.domain.model.primaryColorFor

/**
 * Detects high-level structural patterns in the recent history. These feed
 * the PatternDetector-driven models and are surfaced in the "AI Explanation"
 * card as user-readable pattern strings.
 *
 * Everything here is intentionally pure and synchronous — callers are expected
 * to invoke on a background dispatcher.
 */
object PatternDetector {

    /**
     * Detects mirror patterns: short sequences of length L whose reverse also
     * occurs immediately afterward (e.g. 1,2,3,3,2,1). Returns counts per length.
     */
    fun mirrorPatterns(history: List<Int>, maxLen: Int = 5): Map<Int, Int> {
        val counts = mutableMapOf<Int, Int>()
        for (L in 2..maxLen) {
            if (history.size < 2 * L) continue
            var c = 0
            for (i in 0..history.size - 2 * L) {
                val a = history.subList(i, i + L)
                val b = history.subList(i + L, i + 2 * L).reversed()
                if (a == b) c++
            }
            if (c > 0) counts[L] = c
        }
        return counts
    }

    /** Detects alternating-color runs of length >= 4 (R,G,R,G,R,G...). */
    fun alternatingColorRuns(history: List<Int>): List<Run> {
        if (history.size < 2) return emptyList()
        val out = mutableListOf<Run>()
        var curColor = primaryColorFor(history.first())
        var curRun = 1
        var alternating = false
        for (i in 1 until history.size) {
            val c = primaryColorFor(history[i])
            if (c != curColor) {
                curRun++
                alternating = true
                curColor = c
            } else {
                if (alternating && curRun >= 4) {
                    out += Run(curColor, curRun, endedAtRoundId = (i - 1).toLong())
                }
                curRun = 1
                alternating = false
                curColor = c
            }
        }
        if (alternating && curRun >= 4) {
            out += Run(curColor, curRun, endedAtRoundId = (history.size - 1).toLong())
        }
        return out
    }

    /**
     * Detects repeating number sequences of length 2..4 (e.g. "3,7" appearing
     * back-to-back: 3,7,3,7). Returns (pattern, count) pairs sorted by count.
     */
    fun repeatingSequences(history: List<Int>): List<Pair<List<Int>, Int>> {
        val counts = mutableMapOf<List<Int>, Int>()
        for (L in 2..4) {
            for (i in 0..history.size - 2 * L) {
                val a = history.subList(i, i + L)
                val b = history.subList(i + L, i + 2 * L)
                if (a == b) counts[a] = (counts[a] ?: 0) + 1
            }
        }
        return counts.entries.sortedByDescending { it.value }.take(8).map { it.key to it.value }
    }

    /**
     * Detects cycles: an exact-length substring that appears more than once.
     * Returns the top-K cycles sorted by occurrence count.
     */
    fun cycles(history: List<Int>, maxLen: Int = 6, topK: Int = 6): List<Cycle> {
        val counts = mutableMapOf<List<Int>, Int>()
        for (L in 2..maxLen) {
            for (i in 0..history.size - L) {
                val key = history.subList(i, i + L).toList()
                counts[key] = (counts[key] ?: 0) + 1
            }
        }
        return counts.entries
            .filter { it.value >= 2 }
            .sortedByDescending { it.value * it.key.size } // weight by length × count
            .take(topK)
            .map { Cycle(length = it.key.size, pattern = it.key, occurrences = it.value) }
    }

    /**
     * True if the current streak of the same primary color has reached or
     * exceeded `threshold`. Useful for "rare event" detection.
     */
    fun longColorStreak(history: List<Int>, threshold: Int = 6): Int {
        if (history.isEmpty()) return 0
        var run = 1
        for (i in history.size - 1 downTo 1) {
            if (primaryColorFor(history[i]) == primaryColorFor(history[i - 1])) run++
            else break
        }
        return if (run >= threshold) run else 0
    }

    /**
     * Returns a single human-readable string summarising the dominant pattern
     * in the recent window. Used verbatim by the "AI Explanation" card.
     */
    fun summarise(history: List<Int>): String {
        if (history.size < 5) return "Insufficient history for pattern detection."
        val sb = StringBuilder()
        val cycles = cycles(history, topK = 1)
        if (cycles.isNotEmpty()) {
            val c = cycles.first()
            sb.append("Cycle ${c.pattern} repeated ${c.occurrences}× in recent history. ")
        }
        val mirrors = mirrorPatterns(history)
        if (mirrors.isNotEmpty()) {
            sb.append("Mirror pattern(s) at length(s) ${mirrors.keys.sorted()}. ")
        }
        val streak = longColorStreak(history)
        if (streak > 0) {
            sb.append("Long color streak of $streak detected — rare event. ")
        }
        val alts = alternatingColorRuns(history)
        if (alts.isNotEmpty()) {
            sb.append("Alternating-color run of length ${alts.maxOf { it.length }} observed. ")
        }
        val repeats = repeatingSequences(history)
        if (repeats.isNotEmpty()) {
            sb.append("Most-repeated short sequence: ${repeats.first().first}. ")
        }
        if (sb.isEmpty()) sb.append("No strong structural pattern in recent window.")
        return sb.toString().trim()
    }
}
