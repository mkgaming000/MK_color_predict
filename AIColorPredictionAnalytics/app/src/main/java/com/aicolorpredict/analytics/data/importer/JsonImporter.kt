package com.aicolorpredict.analytics.data.importer

import com.aicolorpredict.analytics.domain.model.Round
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

/**
 * JSON importer. Accepts either a JSON array of round objects or a wrapper
 * `{"rounds": [...]}`. Each round object supports:
 *
 *   { "id": 1, "time": "2024-01-01T00:00:00Z", "number": 4 }
 *
 * Or the wide form with explicit previous windows. As with the CSV importer,
 * missing windows are derived from the order of the array.
 */
object JsonImporter {

    fun parse(stream: InputStream): List<Round> {
        val text = stream.bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val arr = if (root.has("rounds")) root.getJSONArray("rounds") else JSONArray(text)
        val out = mutableListOf<Round>()
        val numberOrder = mutableListOf<Int>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val n = obj.optInt("number", -1)
            if (n !in 0..9) continue
            val id = obj.optLong("id", (i + 1).toLong())
            val ms = parseTime(obj.opt("time") ?: obj.opt("timestamp") ?: obj.opt("epochMs"))
            val prevWindowExplicit = obj.optJSONArray("previousNumbers")
            val prior = if (prevWindowExplicit != null) {
                (0 until prevWindowExplicit.length()).mapNotNull { prevWindowExplicit.optInt(it, -1).takeIf { v -> v in 0..9 } }
            } else {
                numberOrder.toList().takeLast(1000)
            }
            out += Round.fromNumber(id = id, epochMs = ms, number = n, prior = prior)
            numberOrder += n
        }
        return out
    }

    private fun parseTime(v: Any): Long = when (v) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull()
            ?: runCatching { com.aicolorpredict.analytics.util.DateUtils.fromIsoUtc(v) }
                .getOrDefault(System.currentTimeMillis())
        else -> System.currentTimeMillis()
    }
}
