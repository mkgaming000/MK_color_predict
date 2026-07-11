package com.aicolorpredict.analytics.data.importer

import com.aicolorpredict.analytics.domain.model.Round
import java.io.InputStream

/**
 * CSV importer.
 *
 * Accepts both "wide" CSVs (one row per round with explicit PreviousRound / Previous3
 * / Previous5 / ... / Previous1000 columns, as in the spec) and "narrow" CSVs that
 * just have [roundId, time, number]. In the narrow case we derive the previous
 * windows from the order of the rows — this is the common case for real exports.
 *
 * Header is case-insensitive. Numeric timestamps may be epoch millis OR ISO-8601.
 */
object CsvImporter {

    fun parse(stream: InputStream): List<Round> {
        val reader = stream.bufferedReader()
        val header = reader.readLine()?.split(',')?.map { it.trim().lowercase() }
            ?: return emptyList()

        val idxOf = { name: String -> header.indexOf(name).takeIf { it >= 0 } }

        val idIdx = idxOf("roundid") ?: idxOf("id")
        val timeIdx = idxOf("time") ?: idxOf("timestamp") ?: idxOf("epochms")
        val numIdx = idxOf("number") ?: idxOf("num") ?: idxOf("result")
        val prevIdx = idxOf("previousround") ?: idxOf("previous")
        val prev3 = idxOf("previous3")
        val prev5 = idxOf("previous5")
        val prev10 = idxOf("previous10")
        val prev20 = idxOf("previous20")
        val prev50 = idxOf("previous50")
        val prev100 = idxOf("previous100")
        val prev500 = idxOf("previous500")
        val prev1000 = idxOf("previous1000")

        if (numIdx == null) throw IllegalArgumentException("CSV must have a 'number' column")

        // We collect raw rows first, then derive windows if missing.
        data class RawRow(val id: Long?, val ms: Long, val number: Int, val prev: Int?, val w3: List<Int>?, val w5: List<Int>?, val w10: List<Int>?, val w20: List<Int>?, val w50: List<Int>?, val w100: List<Int>?, val w500: List<Int>?, val w1000: List<Int>?)

        val rows = mutableListOf<RawRow>()
        val numberOrder = mutableListOf<Int>()  // in input order

        reader.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            val cols = parseCsvLine(line)
            if (cols.size <= numIdx) return@forEachLine

            val n = cols[numIdx].trim().toIntOrNull() ?: return@forEachLine
            if (n !in 0..9) return@forEachLine

            val id = idIdx?.let { cols.getOrNull(it)?.trim()?.toLongOrNull() }
            val ms = parseTime(cols.getOrNull(timeIdx ?: -1))
            val prev = prevIdx?.let { cols.getOrNull(it)?.trim()?.toIntOrNull() }

            fun win(i: Int?): List<Int>? = i?.let { idx ->
                cols.getOrNull(idx)?.takeIf { it.isNotBlank() }?.split('|')?.mapNotNull { it.trim().toIntOrNull() }
            }

            rows += RawRow(id, ms, n, prev, win(prev3), win(prev5), win(prev10), win(prev20), win(prev50), win(prev100), win(prev500), win(prev1000))
            numberOrder += n
        }

        // Now build rounds. If any window is missing we fall back to numberOrder.
        return rows.mapIndexed { i, row ->
            val prior = if (row.prev != null) {
                // Best effort: synthesize from "previous" plus what we know
                val tail = numberOrder.take(i).takeLast(1000)
                tail
            } else {
                numberOrder.take(i).takeLast(1000)
            }
            Round.fromNumber(
                id = row.id ?: (i + 1).toLong(),
                epochMs = row.ms,
                number = row.number,
                prior = prior
            )
        }
    }

    private fun parseTime(raw: String?): Long {
        if (raw == null) return System.currentTimeMillis()
        val trimmed = raw.trim()
        trimmed.toLongOrNull()?.let { return it }
        // ISO-8601 fallback
        return runCatching { com.aicolorpredict.analytics.util.DateUtils.fromIsoUtc(trimmed) }
            .getOrDefault(System.currentTimeMillis())
    }

    /** Minimal RFC-4180 CSV line parser (handles quoted fields). */
    private fun parseCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    sb.append('"'); i += 2; continue
                }
                c == '"' -> { inQuotes = !inQuotes; i++; continue }
                c == ',' && !inQuotes -> { out += sb.toString(); sb.clear(); i++; continue }
                else -> { sb.append(c); i++ }
            }
        }
        out += sb.toString()
        return out
    }
}
