package com.aicolorpredict.analytics.data.backup

import com.aicolorpredict.analytics.data.local.entity.ModelPerformanceEntity
import com.aicolorpredict.analytics.data.local.entity.PredictionEntity
import com.aicolorpredict.analytics.data.local.entity.RoundEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

/**
 * Full backup format — bundles rounds + predictions + model_performance into a
 * single JSON file. Used by the Data screen's "Backup" / "Restore" actions.
 *
 * The format is intentionally versioned so future schema migrations can stay
 * backward compatible.
 */
object BackupManager {

    private const val VERSION = 1

    data class Bundle(
        val rounds: List<RoundEntity>,
        val predictions: List<PredictionEntity>,
        val performance: List<ModelPerformanceEntity>
    )

    fun write(bundle: Bundle, out: OutputStream) {
        val root = JSONObject()
        root.put("version", VERSION)
        root.put("createdAt", System.currentTimeMillis())

        val r = JSONArray()
        for (e in bundle.rounds) {
            val o = JSONObject()
            o.put("id", e.id); o.put("epochMs", e.epochMs); o.put("number", e.number)
            o.put("colors", e.colors); o.put("previousNumber", e.previousNumber ?: JSONObject.NULL)
            o.put("previous3", e.previous3); o.put("previous5", e.previous5)
            o.put("previous10", e.previous10); o.put("previous20", e.previous20)
            o.put("previous50", e.previous50); o.put("previous100", e.previous100)
            o.put("previous500", e.previous500); o.put("previous1000", e.previous1000)
            o.put("streak", e.streak); o.put("isOdd", e.isOdd); o.put("isEven", e.isEven)
            o.put("isSmall", e.isSmall); o.put("isBig", e.isBig); o.put("isGreen", e.isGreen)
            o.put("isRed", e.isRed); o.put("isViolet", e.isViolet)
            r.put(o)
        }
        root.put("rounds", r)

        val p = JSONArray()
        for (e in bundle.predictions) {
            val o = JSONObject()
            o.put("id", e.id); o.put("roundId", e.roundId); o.put("epochMs", e.epochMs)
            o.put("modelName", e.modelName); o.put("topPick", e.topPick)
            o.put("topProbability", e.topProbability); o.put("confidence", e.confidence)
            o.put("reason", e.reason)
            o.put("numberProbabilities", e.numberProbabilities)
            o.put("colorProbabilities", e.colorProbabilities)
            o.put("actualOutcome", e.actualOutcome ?: JSONObject.NULL)
            o.put("correct", e.correct ?: JSONObject.NULL)
            p.put(o)
        }
        root.put("predictions", p)

        val m = JSONArray()
        for (e in bundle.performance) {
            val o = JSONObject()
            o.put("modelName", e.modelName); o.put("samplesObserved", e.samplesObserved)
            o.put("top1Accuracy", e.top1Accuracy); o.put("top3Accuracy", e.top3Accuracy)
            o.put("top5Accuracy", e.top5Accuracy); o.put("logLoss", e.logLoss)
            o.put("brierScore", e.brierScore); o.put("precision", e.precision)
            o.put("recall", e.recall); o.put("f1", e.f1)
            o.put("confusionMatrixCsv", e.confusionMatrixCsv)
            o.put("rollingAccuracy", e.rollingAccuracy); o.put("rollingWindow", e.rollingWindow)
            o.put("lastUpdated", e.lastUpdated)
            m.put(o)
        }
        root.put("modelPerformance", m)

        out.bufferedWriter().use { it.write(root.toString(2)) }
    }

    fun read(input: InputStream): Bundle {
        val text = input.bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val version = root.optInt("version", 1)
        require(version <= VERSION) { "Unsupported backup version: $version" }

        val rounds = mutableListOf<RoundEntity>()
        val rArr = root.optJSONArray("rounds") ?: JSONArray()
        for (i in 0 until rArr.length()) {
            val o = rArr.getJSONObject(i)
            rounds += RoundEntity(
                id = o.getLong("id"),
                epochMs = o.getLong("epochMs"),
                number = o.getInt("number"),
                colors = o.getInt("colors"),
                previousNumber = if (o.isNull("previousNumber")) null else o.getInt("previousNumber"),
                previous3 = o.optString("previous3"),
                previous5 = o.optString("previous5"),
                previous10 = o.optString("previous10"),
                previous20 = o.optString("previous20"),
                previous50 = o.optString("previous50"),
                previous100 = o.optString("previous100"),
                previous500 = o.optString("previous500"),
                previous1000 = o.optString("previous1000"),
                streak = o.getInt("streak"),
                isOdd = o.getBoolean("isOdd"),
                isEven = o.getBoolean("isEven"),
                isSmall = o.getBoolean("isSmall"),
                isBig = o.getBoolean("isBig"),
                isGreen = o.getBoolean("isGreen"),
                isRed = o.getBoolean("isRed"),
                isViolet = o.getBoolean("isViolet")
            )
        }

        val predictions = mutableListOf<PredictionEntity>()
        val pArr = root.optJSONArray("predictions") ?: JSONArray()
        for (i in 0 until pArr.length()) {
            val o = pArr.getJSONObject(i)
            predictions += PredictionEntity(
                id = o.getLong("id"),
                roundId = o.getLong("roundId"),
                epochMs = o.getLong("epochMs"),
                modelName = o.getString("modelName"),
                topPick = o.getInt("topPick"),
                topProbability = o.getDouble("topProbability"),
                confidence = o.getDouble("confidence"),
                reason = o.getString("reason"),
                numberProbabilities = o.getString("numberProbabilities"),
                colorProbabilities = o.getString("colorProbabilities"),
                actualOutcome = if (o.isNull("actualOutcome")) null else o.getInt("actualOutcome"),
                correct = if (o.isNull("correct")) null else o.getInt("correct")
            )
        }

        val perf = mutableListOf<ModelPerformanceEntity>()
        val mArr = root.optJSONArray("modelPerformance") ?: JSONArray()
        for (i in 0 until mArr.length()) {
            val o = mArr.getJSONObject(i)
            perf += ModelPerformanceEntity(
                modelName = o.getString("modelName"),
                samplesObserved = o.getInt("samplesObserved"),
                top1Accuracy = o.getDouble("top1Accuracy"),
                top3Accuracy = o.getDouble("top3Accuracy"),
                top5Accuracy = o.getDouble("top5Accuracy"),
                logLoss = o.getDouble("logLoss"),
                brierScore = o.getDouble("brierScore"),
                precision = o.getDouble("precision"),
                recall = o.getDouble("recall"),
                f1 = o.getDouble("f1"),
                confusionMatrixCsv = o.getString("confusionMatrixCsv"),
                rollingAccuracy = o.getDouble("rollingAccuracy"),
                rollingWindow = o.getInt("rollingWindow"),
                lastUpdated = o.getLong("lastUpdated")
            )
        }

        return Bundle(rounds = rounds, predictions = predictions, performance = perf)
    }
}
