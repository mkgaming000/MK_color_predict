package com.aicolorpredict.analytics.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persistent representation of a color round.
 *
 * - [color] is 0 (RED) or 1 (GREEN).
 * - [previousColor] is -1 if this is the first round, otherwise 0 or 1.
 * - Indexed on [timestamp] for time-range queries and [color] for filtering.
 */
@Entity(
    tableName = "color_rounds",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["color"]),
        Index(value = ["sequenceIndex"])
    ]
)
data class ColorRoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val color: Int,            // 0 = RED, 1 = GREEN
    val previousColor: Int,    // -1 = none, 0 = RED, 1 = GREEN
    val streak: Int,
    val sequenceIndex: Int
)
