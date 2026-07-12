package com.aicolorpredict.analytics.ai.learning

import android.util.Log
import com.aicolorpredict.analytics.ai.base.ModelRegistry
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.AccuracyMetrics
import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.aicolorpredict.analytics.metrics.MetricsCalculator
import com.aicolorpredict.analytics.util.AppDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Historical back-testing engine.
 *
 * Replays the stored prediction-vs-actual pairs and computes honest
 * performance metrics for every model and for the ensemble as a whole.
 *
 * This is NOT a forward simulation — it uses real predictions that were
 * actually made and stored before each round was resolved. The metrics
 * therefore reflect genuine historical performance, not hindsight.
 *
 * Results are returned as a [BacktestReport] that the UI can display
 * directly. All computation runs on [AppDispatchers.default].
 */
@Singleton
class BacktestingEngine @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val registry: ModelRegistry,
    private val dispatchers: AppDispatchers
) {

    data class BacktestReport(
        val totalRoundsTested: Int,
        val systemMetrics: AccuracyMetrics,
        val perModel: List<ModelBacktest>,
        val description: String
    )

    data class ModelBacktest(
        val modelName: String,
        val samples: Int,
        val top1Accuracy: Double,
        val top3Accuracy: Double,
        val top5Accuracy: Double,
        val logLoss: Double,
        val brierScore: Double,
        val precision: Double,
        val recall: Double,
        val f1: Double,
        val rollingAccuracy: Double
    )

    /**
     * Run a full back-test across all resolved predictions.
     *
     * @param maxSamples cap per model (default 1000) to keep latency
     *   predictable for very large histories.
     */
    suspend fun runBacktest(maxSamples: Int = 1000): BacktestReport =
        withContext(dispatchers.default) {
            Log.d("Backtest", "Starting back-test (maxSamples=$maxSamples)")
            val perModel = mutableListOf<ModelBacktest>()
            var totalResolved = 0

            for (name in registry.names) {
                val resolved = predictionRepo.recentResolvedByModel(name, limit = maxSamples)
                if (resolved.isEmpty()) continue

                val outputs = resolved.map { it.first }
                val actuals = resolved.map { it.second }
                totalResolved = maxOf(totalResolved, resolved.size)

                val mb = ModelBacktest(
                    modelName = name,
                    samples = resolved.size,
                    top1Accuracy = MetricsCalculator.top1(outputs, actuals),
                    top3Accuracy = MetricsCalculator.topK(outputs, actuals, 3),
                    top5Accuracy = MetricsCalculator.topK(outputs, actuals, 5),
                    logLoss = MetricsCalculator.logLoss(outputs, actuals),
                    brierScore = MetricsCalculator.brierScore(outputs, actuals),
                    precision = MetricsCalculator.macroPrecision(outputs, actuals),
                    recall = MetricsCalculator.macroRecall(outputs, actuals),
                    f1 = MetricsCalculator.macroF1(
                        MetricsCalculator.macroPrecision(outputs, actuals),
                        MetricsCalculator.macroRecall(outputs, actuals)
                    ),
                    rollingAccuracy = MetricsCalculator.rollingAccuracy(outputs, actuals, 100)
                )
                perModel += mb
                Log.d("Backtest", "$name: top1=${"%.3f".format(mb.top1Accuracy)} samples=${mb.samples}")
            }

            // System-level aggregate
            val systemPerf = perModel.map { mb ->
                com.aicolorpredict.analytics.domain.model.ModelPerformance.EMPTY.copy(
                    modelName = mb.modelName,
                    samplesObserved = mb.samples,
                    top1Accuracy = mb.top1Accuracy,
                    top3Accuracy = mb.top3Accuracy,
                    top5Accuracy = mb.top5Accuracy,
                    logLoss = mb.logLoss,
                    brierScore = mb.brierScore,
                    precision = mb.precision,
                    recall = mb.recall,
                    f1 = mb.f1,
                    rollingAccuracy = mb.rollingAccuracy,
                    rollingWindow = 100,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            val sys = MetricsCalculator.systemMetrics(systemPerf)

            val description = buildString {
                append("Back-test over $totalResolved resolved predictions per model. ")
                append("System Top-1: ${"%.1f".format(sys.top1Accuracy * 100)}%, ")
                append("Top-3: ${"%.1f".format(sys.top3Accuracy * 100)}%, ")
                append("LogLoss: ${if (sys.logLoss.isNaN()) "N/A" else "%.3f".format(sys.logLoss)}, ")
                append("Brier: ${if (sys.brierScore.isNaN()) "N/A" else "%.3f".format(sys.brierScore)}. ")
                if (perModel.isEmpty()) {
                    append("No resolved predictions yet — add rounds and run predictions to populate.")
                } else {
                    append("Best Top-1: ${perModel.maxByOrNull { it.top1Accuracy }?.modelName} ")
                    append("(${"%.1f".format((perModel.maxByOrNull { it.top1Accuracy }?.top1Accuracy ?: 0.0) * 100)}%).")
                }
            }

            Log.d("Backtest", "Complete: ${perModel.size} models, system top1=${"%.3f".format(sys.top1Accuracy)}")
            BacktestReport(totalResolved, sys, perModel.sortedByDescending { it.top1Accuracy }, description)
        }
}
