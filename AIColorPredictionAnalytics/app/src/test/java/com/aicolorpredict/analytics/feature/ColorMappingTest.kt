package com.aicolorpredict.analytics.feature

import com.aicolorpredict.analytics.domain.model.colorsForNumber
import com.aicolorpredict.analytics.domain.model.primaryColorFor
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ColorMappingTest {

    @Test
    fun `zero carries red and violet`() {
        assertThat(colorsForNumber(0)).containsExactly(
            com.aicolorpredict.analytics.domain.model.BallColor.RED,
            com.aicolorpredict.analytics.domain.model.BallColor.VIOLET
        )
    }

    @Test
    fun `five carries green and violet`() {
        assertThat(colorsForNumber(5)).containsExactly(
            com.aicolorpredict.analytics.domain.model.BallColor.GREEN,
            com.aicolorpredict.analytics.domain.model.BallColor.VIOLET
        )
    }

    @Test
    fun `odd numbers are green primary`() {
        for (n in listOf(1, 3, 5, 7, 9)) {
            assertThat(primaryColorFor(n)).isEqualTo(com.aicolorpredict.analytics.domain.model.BallColor.GREEN)
        }
    }

    @Test
    fun `even numbers are red primary`() {
        for (n in listOf(0, 2, 4, 6, 8)) {
            assertThat(primaryColorFor(n)).isEqualTo(com.aicolorpredict.analytics.domain.model.BallColor.RED)
        }
    }

    @Test
    fun `every number has at most two colors`() {
        for (n in 0..9) {
            assertThat(colorsForNumber(n).size).isAtMost(2)
        }
    }
}
