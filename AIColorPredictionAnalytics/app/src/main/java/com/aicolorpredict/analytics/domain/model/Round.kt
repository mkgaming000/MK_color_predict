package com.aicolorpredict.analytics.domain.model

/**
 * A historical game round.
 *
 * - [previousWindowN] maps window size -> ordered (most-recent-last) list of past numbers.
 *   We keep windows as separate fields so the database column layout matches the spec, but
 *   the [previousWindows] accessor returns them in a fixed order for feature engineers.
 *
 * - [streak] is the run-length of consecutive identical colors *ending* at this round
 *   (i.e. "Red has come up 4 times in a row, this is the 4th"). Positive = same color,
 *   zero = color changed from previous round.
 *
 * All numeric fields are validated at construction; [Round] is the single source of truth
 * for downstream consumers.
 */
data class Round(
    val id: Long,
    val epochMs: Long,
    val number: Int,
    val colors: Set<BallColor>,
    val previousNumber: Int?,
    val previous3: List<Int>,
    val previous5: List<Int>,
    val previous10: List<Int>,
    val previous20: List<Int>,
    val previous50: List<Int>,
    val previous100: List<Int>,
    val previous500: List<Int>,
    val previous1000: List<Int>,
    val streak: Int,
    val isOdd: Boolean,
    val isEven: Boolean,
    val isSmall: Boolean,
    val isBig: Boolean,
    val isGreen: Boolean,
    val isRed: Boolean,
    val isViolet: Boolean
) {
    val primaryColor: BallColor get() = if (isRed) BallColor.RED else BallColor.GREEN

    /** Ordered list of (windowSize, samples) pairs, ascending by window size. */
    val previousWindows: List<Pair<Int, List<Int>>>
        get() = listOf(
            3 to previous3, 5 to previous5, 10 to previous10,
            20 to previous20, 50 to previous50, 100 to previous100,
            500 to previous500, 1000 to previous1000
        )

    companion object {
        fun fromNumber(id: Long, epochMs: Long, number: Int, prior: List<Int>): Round {
            require(number in 0..9) { "Number must be 0..9, was $number" }
            val colors = colorsForNumber(number)
            val previousNumber = prior.lastOrNull()
            // Build windows from the tail of `prior` (most recent last).
            fun takeLast(n: Int): List<Int> = if (prior.size >= n) prior.takeLast(n) else prior.toList()
            val prev3 = takeLast(3)
            val prev5 = takeLast(5)
            val prev10 = takeLast(10)
            val prev20 = takeLast(20)
            val prev50 = takeLast(50)
            val prev100 = takeLast(100)
            val prev500 = takeLast(500)
            val prev1000 = takeLast(1000)

            // streak: consecutive same-color run length ending at this round (inclusive).
            val streak = runLengthSameColor(prior, colors)

            return Round(
                id = id,
                epochMs = epochMs,
                number = number,
                colors = colors,
                previousNumber = previousNumber,
                previous3 = prev3,
                previous5 = prev5,
                previous10 = prev10,
                previous20 = prev20,
                previous50 = prev50,
                previous100 = prev100,
                previous500 = prev500,
                previous1000 = prev1000,
                streak = streak,
                isOdd = isOdd(number),
                isEven = isEven(number),
                isSmall = isSmall(number),
                isBig = isBig(number),
                isGreen = colors.contains(BallColor.GREEN),
                isRed = colors.contains(BallColor.RED),
                isViolet = colors.contains(BallColor.VIOLET)
            )
        }

        /**
         * Counts how many consecutive rounds immediately preceding (and including) this
         * round shared the same primary color. Returns >= 1 (this round always counts).
         */
        private fun runLengthSameColor(prior: List<Int>, currentColors: Set<BallColor>): Int {
            val currentPrimary = if (currentColors.contains(BallColor.RED)) BallColor.RED else BallColor.GREEN
            var run = 1
            for (n in prior.reversed()) {
                val p = if (n % 2 == 0) BallColor.RED else BallColor.GREEN
                // 0 and 5 carry Violet but their primary is still RED/GREEN respectively.
                // We treat 0 as RED primary, 5 as GREEN primary — same as primaryColorFor.
                val prim = if (n == 0) BallColor.RED else if (n == 5) BallColor.GREEN else p
                if (prim == currentPrimary) run++ else break
            }
            return run
        }
    }
}
