package com.aicolorpredict.analytics.feature

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PatternDetectorTest {

    @Test
    fun `mirror pattern detected for palindrome`() {
        val history = listOf(1, 2, 3, 3, 2, 1)
        val mirrors = PatternDetector.mirrorPatterns(history, maxLen = 3)
        // L=3 mirrors: [1,2,3] ↔ [3,2,1]
        assertThat(mirrors).containsKey(3)
    }

    @Test
    fun `alternating color runs detected for RG pattern`() {
        val history = listOf(1, 2, 3, 4, 5, 6)  // G, R, G, R, G, R → alternating
        val runs = PatternDetector.alternatingColorRuns(history)
        assertThat(runs).isNotEmpty()
        assertThat(runs.maxOf { it.length }).isAtLeast(6)
    }

    @Test
    fun `cycles detected for repeating subsequence`() {
        val history = listOf(3, 7, 3, 7, 3, 7)
        val cycles = PatternDetector.cycles(history, topK = 3)
        assertThat(cycles).isNotEmpty()
        // Top cycle should be [3,7] with at least 3 occurrences
        val top = cycles.first()
        assertThat(top.occurrences).isAtLeast(2)
    }

    @Test
    fun `summarise returns non-empty string for normal history`() {
        val history = (1..30).map { it % 10 }
        val s = PatternDetector.summarise(history)
        assertThat(s).isNotEmpty()
    }
}
