package com.aicolorpredict.analytics.data.local.entity

import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.Round

/** Helpers for translating between Room entities and domain [Round]s. */

private fun parseWindow(s: String): List<Int> =
    if (s.isBlank()) emptyList()
    else s.split(',').mapNotNull { it.trim().toIntOrNull() }

private fun List<Int>.toCsv(): String = joinToString(",")

fun RoundEntity.toDomain(): Round = Round(
    id = id,
    epochMs = epochMs,
    number = number,
    colors = BallColor.entries.filterTo(mutableSetOf()) {
        when (it) {
            BallColor.RED -> colors and 1 != 0
            BallColor.GREEN -> colors and 2 != 0
            BallColor.VIOLET -> colors and 4 != 0
        }
    },
    previousNumber = previousNumber,
    previous3 = parseWindow(previous3),
    previous5 = parseWindow(previous5),
    previous10 = parseWindow(previous10),
    previous20 = parseWindow(previous20),
    previous50 = parseWindow(previous50),
    previous100 = parseWindow(previous100),
    previous500 = parseWindow(previous500),
    previous1000 = parseWindow(previous1000),
    streak = streak,
    isOdd = isOdd,
    isEven = isEven,
    isSmall = isSmall,
    isBig = isBig,
    isGreen = isGreen,
    isRed = isRed,
    isViolet = isViolet
)

fun Round.toEntity(): RoundEntity = RoundEntity(
    id = id,
    epochMs = epochMs,
    number = number,
    colors = colors.fold(0) { acc, c ->
        acc or when (c) {
            BallColor.RED -> 1
            BallColor.GREEN -> 2
            BallColor.VIOLET -> 4
        }
    },
    previousNumber = previousNumber,
    previous3 = previous3.toCsv(),
    previous5 = previous5.toCsv(),
    previous10 = previous10.toCsv(),
    previous20 = previous20.toCsv(),
    previous50 = previous50.toCsv(),
    previous100 = previous100.toCsv(),
    previous500 = previous500.toCsv(),
    previous1000 = previous1000.toCsv(),
    streak = streak,
    isOdd = isOdd,
    isEven = isEven,
    isSmall = isSmall,
    isBig = isBig,
    isGreen = isGreen,
    isRed = isRed,
    isViolet = isViolet
)
