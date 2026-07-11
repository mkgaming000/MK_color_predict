package com.aicolorpredict.analytics.metrics

import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MetricsCalculatorTest {

    private fun makeOutput(probs: DoubleArray, topPick: Int): ModelOutput {
        val normalised = probs.map { it / probs.sum() }.toDoubleArray()
        return ModelOutput.fromVector(
            modelName = "test",
            raw = normalised,
            confidence = 0.5,
            reason = "test",
            accuracy = 0.5
        ).let {
            // Force topPick (the fromVector picks the argmax automatically; we override for the test)
            if (it.topPick == topPick) it
            else it.copy(topPick = topPick)
        }
    }

    @Test fun `top1 accuracy counts hits only`() {
        val outputs = listOf(
            makeOutput(doubleArrayOf(0.6, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.0), topPick = 0),
            makeOutput(doubleArrayOf(0.6, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.0), topPick = 0),
            makeOutput(doubleArrayOf(0.6, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.0), topPick = 0)
        )
        val actuals = listOf(0, 1, 0)
        assertThat(MetricsCalculator.top1(outputs, actuals)).isWithin(1e-9).of(2.0 / 3.0)
    }

    @Test fun `topK counts hit when actual is in top k by probability`() {
        val probs = doubleArrayOf(0.5, 0.2, 0.1, 0.05, 0.05, 0.05, 0.05, 0.0, 0.0, 0.0)
        // Sorted desc → [0, 1, 2, ...] so top-2 contains 0 and 1.
        val outputs = listOf(makeOutput(probs, topPick = 0))
        val actuals = listOf(1)
        assertThat(MetricsCalculator.topK(outputs, actuals, k = 2)).isWithin(1e-9).of(1.0)
    }

    @Test fun `logloss is non-negative and increases as predicted probability of actual drops`() {
        val output = makeOutput(doubleArrayOf(0.9, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), topPick = 0)
        val llHit = MetricsCalculator.logLoss(listOf(output), listOf(0))
        val llMiss = MetricsCalculator.logLoss(listOf(output), listOf(1))
        assertThat(llHit).isLessThan(llMiss)
        assertThat(llHit).isGreaterThan(0.0)
    }

    @Test fun `brier score is zero for perfect prediction`() {
        val output = makeOutput(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), topPick = 0)
        val bs = MetricsCalculator.brierScore(listOf(output), listOf(0))
        assertThat(bs).isWithin(1e-9).of(0.0)
    }

    @Test fun `brier score is 2 for maximally wrong prediction`() {
        val output = makeOutput(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), topPick = 0)
        val bs = MetricsCalculator.brierScore(listOf(output), listOf(1))
        // (1-0)^2 + (0-1)^2 + 8×(0-0)^2 = 2
        assertThat(bs).isWithin(1e-9).of(2.0)
    }

    @Test fun `confusion matrix is indexed actual then predicted`() {
        val outputs = listOf(
            makeOutput(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), topPick = 0),
            makeOutput(doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), topPick = 1)
        )
        val actuals = listOf(0, 0)
        val cm = MetricsCalculator.confusionMatrix(outputs, actuals)
        // actual=0, predicted=0 → 1; actual=0, predicted=1 → 1
        assertThat(cm[0]!![0]!!).isEqualTo(1)
        assertThat(cm[0]!![1]!!).isEqualTo(1)
    }

    @Test fun `rolling accuracy takes only last N`() {
        val outputs = (1..200).map { makeOutput(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), topPick = 0) }
        val actuals = (1..200).map { 0 }  // all correct
        val ra = MetricsCalculator.rollingAccuracy(outputs, actuals, window = 50)
        assertThat(ra).isWithin(1e-9).of(1.0)
    }

    @Test fun `macro precision and recall on perfect predictions equals 1`() {
        val outputs = (0..9).map { makeOutput(DoubleArray(10) { i -> if (i == it) 1.0 else 0.0 }, topPick = it) }
        val actuals = (0..9).toList()
        val p = MetricsCalculator.macroPrecision(outputs, actuals)
        val r = MetricsCalculator.macroRecall(outputs, actuals)
        assertThat(p).isWithin(1e-9).of(1.0)
        assertThat(r).isWithin(1e-9).of(1.0)
    }

    @Test fun `system metrics aggregates per-model metrics correctly`() {
        val perf1 = com.aicolorpredict.analytics.domain.model.ModelPerformance.EMPTY.copy(
            modelName = "A", samplesObserved = 100, top1Accuracy = 0.4, top3Accuracy = 0.7, top5Accuracy = 0.9,
            logLoss = 2.0, brierScore = 0.3, precision = 0.4, recall = 0.4, f1 = 0.4, rollingAccuracy = 0.45
        )
        val perf2 = com.aicolorpredict.analytics.domain.model.ModelPerformance.EMPTY.copy(
            modelName = "B", samplesObserved = 100, top1Accuracy = 0.2, top3Accuracy = 0.5, top5Accuracy = 0.7,
            logLoss = 2.5, brierScore = 0.4, precision = 0.2, recall = 0.2, f1 = 0.2, rollingAccuracy = 0.25
        )
        val sys = MetricsCalculator.systemMetrics(listOf(perf1, perf2))
        // Weighted by samplesObserved: (0.4*100 + 0.2*100) / 200 = 0.3
        assertThat(sys.top1Accuracy).isWithin(1e-9).of(0.3)
        assertThat(sys.totalPredictions).isEqualTo(200)
    }
}
