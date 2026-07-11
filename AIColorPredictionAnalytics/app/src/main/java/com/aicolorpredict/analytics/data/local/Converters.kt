package com.aicolorpredict.analytics.data.local

import androidx.room.TypeConverter
import com.aicolorpredict.analytics.domain.model.BallColor

/**
 * Converters used by Room to encode domain types into SQLite columns.
 *
 * Color bitmask conventions match [com.aicolorpredict.analytics.data.local.entity.RoundEntity]:
 * RED=1, GREEN=2, VIOLET=4. We expose helpers here so repositories can stay
 * consistent with the on-disk encoding.
 */
class Converters {
    @TypeConverter
    fun fromBallColorSet(value: Set<BallColor>): Int =
        value.fold(0) { acc, c -> acc or c.bitmask() }

    @TypeConverter
    fun toBallColorSet(value: Int): Set<BallColor> {
        val s = mutableSetOf<BallColor>()
        if (value and 1 != 0) s.add(BallColor.RED)
        if (value and 2 != 0) s.add(BallColor.GREEN)
        if (value and 4 != 0) s.add(BallColor.VIOLET)
        return s
    }
}

private fun BallColor.bitmask(): Int = when (this) {
    BallColor.RED -> 1
    BallColor.GREEN -> 2
    BallColor.VIOLET -> 4
}
