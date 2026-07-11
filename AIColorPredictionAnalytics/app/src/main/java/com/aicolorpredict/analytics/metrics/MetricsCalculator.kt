package com.aicolorpredict.analytics.metrics

import com.aicolorpredict.analytics.domain.model.AccuracyMetrics
import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.aicolorpredict.analytics.domain.model.ModelPerformance
import kotlin.math.ln

/**
 * Computes every metric displayed in the UI:
 *
 *   - Top-1 / Top-3 / Top-5 accuracy
 *   - LogLoss (multinomial cross-entropy, with epsilon clamping)
 *   - Brier score (multiclass)
 *   - Macro precision / recall / F1
 *   - Confusion matrix (predicted × actual)
 *   - Rolling accuracy over the last N resolved predictions
 *
 * Every function here is pure — given the same inputs it produces the same
 * outputs. This is the single source of truth for "how well is the system
 * doing?" — no other module is allowed to compute its own accuracy.
 */
object MetricsCalculator {

    data class PerModelBatch(
        val outputs: List<ModelOutput>,
        val actuals: List<Int>
    )

    fun top1(outputs: List<ModelOutput>, actuals: List<Int>): Double {
        require(outputs.size == actuals.size)
        if (outputs.isEmpty()) return 0.0
        val hits = outputs.indices.count { i -> outputs[i].topPick == actuals[i] }
        return hits.toDouble() / outputs.size
    }

    fun topK(outputs: List<ModelOutput>, actuals: List<Int>, k: Int): Double {
        require(outputs.size == actuals.size)
        if (outputs.isEmpty()) return 0.0
        var hits = 0
        for (i in outputs.indices) {
            val topK = outputs[i].numberProbabilities.entries
                .sortedByDescending { it.value }
                .take(k)
                .map { it.key }
            if (actuals[i] in topK) hits++
        }
        return hits.toDouble() / outputs.size
    }

    fun logLoss(outputs: List<ModelOutput>, actuals: List<Int>): Double {
        require(outputs.size == actuals.size)
        if (outputs.isEmpty()) return Double.NaN
        var sum = 0.0
        for (i in outputs.indices) {
            val p = outputs[i].numberProbabilities[actuals[i]]?.coerceIn(1e-12, 1.0) ?: 1e-12
            sum += -ln(p)
        }
        return sum / outputs.size
    }

    fun brierScore(outputs: List<ModelOutput>, actuals: List<Int>): Double {
        require(outputs.size == actuals.size)
        if (outputs.isEmpty()) return Double.NaN
        var sum = 0.0
        for (i in outputs.indices) {
            val actual = actuals[i]
            for (n in 0..9) {
                val forecast = outputs[i].numberProbabilities[n] ?: 0.0
                val obs = if (n == actual) 1.0 else 0.0
                sum += (forecast - obs) * (forecast - obs)
            }
        }
        return sum / outputs.size
    }

    /** Macro precision = average of per-class precision. */
    fun macroPrecision(outputs: List<ModelOutput>, actuals: List<Int>): Double {
        val cm = confusionMatrix(outputs, actuals)
        var sum = 0.0; var count = 0
        for (cls in 0..9) {
            val tp = cm[cls]?.get(cls) ?: 0
            val predicted = (0..9).sumOf { cm[it]?.get(cls) ?: 0 }
            if (predicted > 0) { sum += tp.toDouble() / predicted; count++ }
        }
        return if (count == 0) 0.0 else sum / count
    }

    fun macroRecall(outputs: List<ModelOutput>, actuals: List<Int>): Double {
        val cm = confusionMatrix(outputs, actuals)
        var sum = 0.0; var count = 0
        for (cls in 0..9) {
            val tp = cm[cls]?.get(cls) ?: 0
            val actual = (0..9).sumOf { cm[cls]?.get(it) ?: 0 }
            if (actual > 0) { sum += tp.toDouble() / actual; count++ }
        }
        return if (count == 0) 0.0 else sum / count
    }

    fun macroF1(precision: Double, recall: Double): Double {
        if (precision + recall <= 0) return 0.0
        return 2.0 * precision * recall / (precision + recall)
    }

    /**
     * Confusion matrix indexed as cm[actual][predicted].
     */
    fun confusionMatrix(outputs: List<ModelOutput>, actuals: List<Int>): Map<Int, Map<Int, Int>> {
        val cm = Array(10) { IntArray(10) }
        for (i in outputs.indices) {
            val a = actuals[i]; val p = outputs[i].topPick
            if (a in 0..9 && p in 0..9) cm[a][p]++
        }
        val out = mutableMapOf<Int, MutableMap<Int, Int>>()
        for (a in 0..9) {
            val inner = mutableMapOf<Int, Int>()
            for (p in 0..9) inner[p] = cm[a][p]
            out[a] = inner
        }
        return out
    }

    /** Rolling Top-1 over the last N resolved predictions. */
    fun rollingAccuracy(outputs: List<ModelOutput>, actuals: List<Int>, window: Int = 100): Double {
        require(outputs.size == actuals.size)
        if (outputs.isEmpty()) return 0.0
        val tail = outputs.indices.toList().takeLast(minOf(window, outputs.size))
        var hits = 0
        for (i in tail) if (outputs[i].topPick == actuals[i]) hits++
        return hits.toDouble() / tail.size
    }

    /** Builds the full [ModelPerformance] snapshot for a single model. */
    fun perModelPerformance(
        modelName: String,
        outputs: List<ModelOutput>,
        actuals: List<Int>,
        rollingWindow: Int = 100,
        now: Long = System.currentTimeMillis()
    ): ModelPerformance {
        if (outputs.isEmpty()) return ModelPerformance.EMPTY.copy(modelName = modelName, lastUpdated = now)
        val t1 = top1(outputs, actuals)
        val t3 = topK(outputs, actuals, 3)
        val t5 = topK(outputs, actuals, 5)
        val ll = logLoss(outputs, actuals)
        val bs = brierScore(outputs, actuals)
        val mp = macroPrecision(outputs, actuals)
        val mr = macroRecall(outputs, actuals)
        val f1 = macroF1(mp, mr)
        val cm = confusionMatrix(outputs, actuals)
        val ra = rollingAccuracy(outputs, actuals, rollingWindow)
        return ModelPerformance(
            modelName = modelName,
            samplesObserved = outputs.size,
            top1Accuracy = t1,
            top3Accuracy = t3,
            top5Accuracy = t5,
            logLoss = ll,
            brierScore = bs,
            precision = mp,
            recall = mr,
            f1 = f1,
            confusionMatrix = cm,
            rollingAccuracy = ra,
            rollingWindow = rollingWindow,
            lastUpdated = now
        )
    }

    /** Aggregates per-model metrics into the system-level [AccuracyMetrics] for the dashboard. */
    fun systemMetrics(perModel: List<ModelPerformance>): AccuracyMetrics {
        if (perModel.isEmpty()) return AccuracyMetrics.EMPTY
        // Average across models, weighted by sample count.
        val totalSamples = perModel.sumOf { it.samplesObserved }.coerceAtLeast(1)
        fun weighted(sel: ModelPerformance.() -> Double): Double =
            perModel.sumOf { it.samplesObserved.toDouble() * it.sel() } / totalSamples
        return AccuracyMetrics(
            totalPredictions = totalSamples,
            top1Accuracy = weighted { top1Accuracy },
            top3Accuracy = weighted { top3Accuracy },
            top5Accuracy = weighted { top5Accuracy },
            logLoss = perModel.map { it.logLoss }.filter { !it.isNaN() }.average(),
            brierScore = perModel.map { it.brierScore }.filter { !it.isNaN() }.average(),
            macroPrecision = weighted { precision },
            macroRecall = weighted { recall },
            macroF1 = weighted { f1 },
            rollingAccuracy = weighted { rollingAccuracy },
            rollingWindowSize = perModel.firstOrNull()?.rollingWindow ?: 100,
            confusionMatrix = emptyMap() // System-level confusion is not meaningful — shown per model
        )
    }
}
