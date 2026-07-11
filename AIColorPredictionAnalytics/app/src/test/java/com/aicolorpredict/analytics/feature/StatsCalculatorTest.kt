package com.aicolorpredict.analytics.feature

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StatsCalculatorTest {

    @Test
    fun `entropy of uniform distribution equals ln of vocab size`() {
        val p = DoubleArray(10) { 0.1 }
        val h = StatsCalculator.entropy(p)
        assertThat(h).isWithin(1e-9).of(kotlin.math.ln(10.0))
    }

    @Test
    fun `entropy of degenerate distribution is zero`() {
        val p = DoubleArray(10) { 0.0 }.also { it[3] = 1.0 }
        val h = StatsCalculator.entropy(p)
        assertThat(h).isWithin(1e-12).of(0.0)
    }

    @Test
    fun `variance of constant array is zero`() {
        val xs = DoubleArray(20) { 5.0 }
        assertThat(StatsCalculator.variance(xs)).isWithin(1e-12).of(0.0)
    }

    @Test
    fun `correlation of identical arrays is one`() {
        val a = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0)
        assertThat(StatsCalculator.correlation(a, a)).isWithin(1e-9).of(1.0)
    }

    @Test
    fun `correlation of oppositely-sorted arrays is minus one`() {
        val a = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val b = doubleArrayOf(5.0, 4.0, 3.0, 2.0, 1.0)
        assertThat(StatsCalculator.correlation(a, b)).isWithin(1e-9).of(-1.0)
    }

    @Test
    fun `cosine of orthogonal vectors is zero`() {
        val a = doubleArrayOf(1.0, 0.0)
        val b = doubleArrayOf(0.0, 1.0)
        assertThat(StatsCalculator.cosine(a, b)).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `run-length encoding collapses consecutive duplicates`() {
        val rle = StatsCalculator.runLengthEncode(listOf(1, 1, 1, 2, 2, 3))
        assertThat(rle).containsExactly(1 to 3, 2 to 2, 3 to 1).inOrder()
    }

    @Test
    fun `linearly-weighted average favours recent values`() {
        val xs = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0)  // recent is 1.0
        val avg = StatsCalculator.linearlyWeightedAverage(xs)
        // Average must be > simple mean (0.2) because recent is weighted higher
        assertThat(avg).isGreaterThan(0.2)
    }
}
