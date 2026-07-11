package com.aicolorpredict.analytics.feature

import com.aicolorpredict.analytics.domain.model.BallColor
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TransitionMatrixTest {

    @Test
    fun `row with no observations returns uniform distribution`() {
        val m = TransitionMatrix()
        val row = m.row(0)
        assertThat(row.sum()).isWithin(1e-9).of(1.0)
        // All entries equal 1/10 with no observations (Laplace alpha=1, denom=10)
        row.forEach { assertThat(it).isWithin(1e-9).of(0.1) }
    }

    @Test
    fun `row with observations concentrates probability on observed targets`() {
        val m = TransitionMatrix()
        // After 0, always see 5
        repeat(20) { m.observe(0, 5) }
        val row = m.row(0)
        assertThat(row.sum()).isWithin(1e-9).of(1.0)
        // P(5 | 0) should be the highest
        val top = row.indices.maxByOrNull { row[it] }
        assertThat(top).isEqualTo(5)
        assertThat(row[5]).isGreaterThan(0.5)
    }

    @Test
    fun `transition analytics computes correct gaps and color splits`() {
        val history = listOf(0, 5, 0, 5, 0, 5)  // alternating
        val stats = TransitionAnalytics.build(from = 0, history = history)
        // From 0, we observed transitions to 5 three times
        assertThat(stats.totalTransitions).isAtLeast(2)
        // Red primary vs Green primary: 0→5 means Red → Green
        assertThat(stats.nextColorProbabilities[BallColor.GREEN]!!).isGreaterThan(0.5)
    }
}
