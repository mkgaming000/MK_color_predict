package com.aicolorpredict.analytics.domain.model

/**
 * Single color outcome for a number 0-9, following the canonical color mapping:
 *
 *   0 = Red + Violet       (purple-red)
 *   1 = Green
 *   2 = Red
 *   3 = Green
 *   4 = Red
 *   5 = Green + Violet     (purple-green)
 *   6 = Red
 *   7 = Green
 *   8 = Red
 *   9 = Green
 *
 * Violet is a *secondary* color flag — every number has exactly one primary color
 * (Red or Green) and may additionally have Violet. We model this with a set so the
 * UI and the models can reason about each color independently.
 */
enum class BallColor(val display: String) {
    RED("Red"),
    GREEN("Green"),
    VIOLET("Violet");

    companion object {
        val PRIMARY = setOf(RED, GREEN)
    }
}

/**
 * Pure mapping from number 0..9 to its set of colors.
 *
 * The result is a [Set] because some numbers (0 and 5) carry two colors
 * (primary + Violet). Callers that need a single primary color should use
 * [primaryColorFor].
 */
fun colorsForNumber(n: Int): Set<BallColor> = when (n) {
    0 -> setOf(BallColor.RED, BallColor.VIOLET)
    1 -> setOf(BallColor.GREEN)
    2 -> setOf(BallColor.RED)
    3 -> setOf(BallColor.GREEN)
    4 -> setOf(BallColor.RED)
    5 -> setOf(BallColor.GREEN, BallColor.VIOLET)
    6 -> setOf(BallColor.RED)
    7 -> setOf(BallColor.GREEN)
    8 -> setOf(BallColor.RED)
    9 -> setOf(BallColor.GREEN)
    else -> throw IllegalArgumentException("Number must be 0..9, was $n")
}

/**
 * The dominant color used for parity-style aggregations. 0 is treated as Red
 * (its primary), 5 is treated as Green (its primary).
 */
fun primaryColorFor(n: Int): BallColor = when (n) {
    0, 2, 4, 6, 8 -> BallColor.RED
    1, 3, 5, 7, 9 -> BallColor.GREEN
    else -> throw IllegalArgumentException("Number must be 0..9, was $n")
}

fun isOdd(n: Int): Boolean = n % 2 == 1
fun isEven(n: Int): Boolean = n % 2 == 0
fun isSmall(n: Int): Boolean = n in 0..4
fun isBig(n: Int): Boolean = n in 5..9

/** Returns true if the number carries the given color (including Violet as a flag). */
fun numberHasColor(n: Int, c: BallColor): Boolean = colorsForNumber(n).contains(c)
