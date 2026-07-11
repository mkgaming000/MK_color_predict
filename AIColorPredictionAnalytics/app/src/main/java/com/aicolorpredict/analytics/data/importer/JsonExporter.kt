package com.aicolorpredict.analytics.data.importer

import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.util.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream

object JsonExporter {

    fun write(rounds: List<Round>, out: OutputStream) {
        val arr = JSONArray()
        for (r in rounds) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("time", DateUtils.toIsoUtc(r.epochMs))
            obj.put("number", r.number)
            obj.put("colors", JSONArray(r.colors.map { it.display }))
            obj.put("previousNumber", r.previousNumber ?: JSONObject.NULL)
            obj.put("previous3", JSONArray(r.previous3))
            obj.put("previous5", JSONArray(r.previous5))
            obj.put("previous10", JSONArray(r.previous10))
            obj.put("previous20", JSONArray(r.previous20))
            obj.put("previous50", JSONArray(r.previous50))
            obj.put("previous100", JSONArray(r.previous100))
            obj.put("previous500", JSONArray(r.previous500))
            obj.put("previous1000", JSONArray(r.previous1000))
            obj.put("streak", r.streak)
            obj.put("odd", r.isOdd)
            obj.put("even", r.isEven)
            obj.put("small", r.isSmall)
            obj.put("big", r.isBig)
            obj.put("green", r.isGreen)
            obj.put("red", r.isRed)
            obj.put("violet", r.isViolet)
            arr.put(obj)
        }
        val root = JSONObject()
        root.put("rounds", arr)
        root.put("exportedAt", DateUtils.toIsoUtc(System.currentTimeMillis()))
        out.bufferedWriter().use { it.write(root.toString(2)) }
    }
}
