package com.aicolorpredict.analytics.domain

import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.domain.model.colorsForNumber
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RoundTest {

    @Test fun `fromNumber sets correct color flags for 0`() {
        val r = Round.fromNumber(id = 1, epochMs = 0L, number = 0, prior = emptyList())
        assertThat(r.isRed).isTrue()
        assertThat(r.isViolet).isTrue()
        assertThat(r.isGreen).isFalse()
        assertThat(r.isEven).isTrue()
        assertThat(r.isSmall).isTrue()
    }

    @Test fun `fromNumber sets correct color flags for 5`() {
        val r = Round.fromNumber(id = 1, epochMs = 0L, number = 5, prior = emptyList())
        assertThat(r.isGreen).isTrue()
        assertThat(r.isViolet).isTrue()
        assertThat(r.isRed).isFalse()
        assertThat(r.isOdd).isTrue()
        assertThat(r.isBig).isTrue()
    }

    @Test fun `fromNumber sets correct color flags for 7`() {
        val r = Round.fromNumber(id = 1, epochMs = 0L, number = 7, prior = emptyList())
        assertThat(r.isGreen).isTrue()
        assertThat(r.isRed).isFalse()
        assertThat(r.isViolet).isFalse()
        assertThat(r.isOdd).isTrue()
        assertThat(r.isBig).isTrue()
    }

    @Test fun `previous windows slice correctly from prior`() {
        val prior = (1..50).toList()
        val r = Round.fromNumber(id = 1, epochMs = 0L, number = 3, prior = prior)
        assertThat(r.previousNumber).isEqualTo(50)
        assertThat(r.previous3).hasSize(3)
        assertThat(r.previous3.last()).isEqualTo(50)
        assertThat(r.previous10).hasSize(10)
        assertThat(r.previous50).hasSize(50)
    }

    @Test fun `streak counts consecutive same-color rounds`() {
        // All even numbers → all red-primary → streak = 4 (the 3 priors + this one)
        val r = Round.fromNumber(id = 1, epochMs = 0L, number = 4, prior = listOf(2, 6, 8))
        assertThat(r.streak).isEqualTo(4)
    }

    @Test fun `streak resets when color changes`() {
        // 3 reds then 1 green then this round (which is red) — streak should be 1
        val r = Round.fromNumber(id = 1, epochMs = 0L, number = 4, prior = listOf(2, 6, 8, 1))
        assertThat(r.streak).isEqualTo(1)
    }

    @Test fun `colors matches canonical mapping for every number`() {
        for (n in 0..9) {
            val r = Round.fromNumber(id = n.toLong(), epochMs = 0L, number = n, prior = emptyList())
            assertThat(r.colors).isEqualTo(colorsForNumber(n))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromNumber rejects out-of-range number`() {
        Round.fromNumber(id = 1, epochMs = 0L, number = 11, prior = emptyList())
    }
}
