package com.aicolorpredict.analytics.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val isoUtc = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    private val display = ThreadLocal.withInitial {
        SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    }
    private val displayShort = ThreadLocal.withInitial {
        SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
    }

    fun toIsoUtc(epochMs: Long): String = isoUtc.get().format(Date(epochMs))
    fun fromIsoUtc(s: String): Long =
        runCatching { isoUtc.get().parse(s)?.time ?: 0L }.getOrDefault(0L)

    fun display(epochMs: Long): String = display.get().format(Date(epochMs))
    fun displayShort(epochMs: Long): String = displayShort.get().format(Date(epochMs))
}
