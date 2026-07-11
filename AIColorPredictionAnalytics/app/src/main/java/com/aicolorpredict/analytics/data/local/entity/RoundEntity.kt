package com.aicolorpredict.analytics.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persistent representation of a historical round.
 *
 * - The previous windows are stored as CSV strings (e.g. "1,2,3,4,5") rather than as
 *   foreign-key relations — this trades write amplification for fast bulk scans, which
 *   is the right call here because the windows are write-once-read-many.
 * - [colors] is stored as a bitmask: RED=1, GREEN=2, VIOLET=4. Compact & indexable.
 * - [epochMs] is indexed so the history screen can paginate by time.
 */
@Entity(
    tableName = "rounds",
    indices = [
        Index(value = ["epochMs"]),
        Index(value = ["number"]),
        Index(value = ["colors"]),
        Index(value = ["streak"])
    ]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val epochMs: Long,
    val number: Int,
    val colors: Int,                  // bitmask: RED=1, GREEN=2, VIOLET=4
    val previousNumber: Int?,
    val previous3: String,
    val previous5: String,
    val previous10: String,
    val previous20: String,
    val previous50: String,
    val previous100: String,
    val previous500: String,
    val previous1000: String,
    val streak: Int,
    val isOdd: Boolean,
    val isEven: Boolean,
    val isSmall: Boolean,
    val isBig: Boolean,
    val isGreen: Boolean,
    val isRed: Boolean,
    val isViolet: Boolean
)
