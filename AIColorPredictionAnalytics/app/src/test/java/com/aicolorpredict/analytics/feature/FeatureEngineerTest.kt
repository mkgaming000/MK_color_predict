package com.aicolorpredict.analytics.feature

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FeatureEngineerTest {

    @Test
    fun `feature set has correct dimensions for small history`() {
        val eng = FeatureEngineer()
        val fs = eng.build(roundId = 1L, history = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0))
        assertThat(fs.totalSamples).isEqualTo(10)
        assertThat(fs.numberFrequency.size).isEqualTo(10)
        assertThat(fs.colorFrequency.size).isEqualTo(3)
        assertThat(fs.transitions.size).isEqualTo(10)
        assertThat(fs.gaps.size).isEqualTo(10)
        assertThat(fs.featureVector.size).isAtLeast(150)
    }

    @Test
    fun `feature vector sums of normalised frequencies equals 1`() {
        val eng = FeatureEngineer()
        val fs = eng.build(roundId = 1L, history = (0..9).toList() + (0..9).toList())
        // The first 10 entries of featureVector are normalised frequencies — they should sum to 1.
        val sumFirst10 = fs.featureVector.take(10).sum()
        assertThat(sumFirst10).isWithin(1e-9).of(1.0)
    }

    @Test
    fun `hot numbers contain the most frequent values`() {
        val eng = FeatureEngineer()
        val history = mutableListOf<Int>()
        // Number 7 appears 5 times, others 0-1 times
        repeat(5) { history.add(7) }
        history.addAll(listOf(1, 2, 3))
        val fs = eng.build(roundId = 1L, history = history)
        assertThat(fs.hotNumbers).contains(7)
    }

    @Test
    fun `entropy of uniform history is close to ln 10`() {
        val eng = FeatureEngineer()
        val fs = eng.build(roundId = 1L, history = (0..9).toList().shuffled())
        assertThat(fs.entropy).isWithin(0.3).of(kotlin.math.ln(10.0))
    }

    @Test
    fun `empty history does not crash`() {
        val eng = FeatureEngineer()
        val fs = eng.build(roundId = 1L, history = emptyList())
        assertThat(fs.totalSamples).isEqualTo(0)
        // Probabilities should be uniform fallback
        assertThat(fs.recentMomentum.values.first()).isWithin(1e-9).of(0.1)
    }
}
