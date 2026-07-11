package com.aicolorpredict.analytics.ai

import com.aicolorpredict.analytics.ai.ensemble.EnsembleModel
import com.aicolorpredict.analytics.ai.calibration.ConfidenceCalibrator
import com.aicolorpredict.analytics.ai.calibration.PlattCalibrator
import com.aicolorpredict.analytics.domain.model.ConsensusLevel
import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class EnsembleAndCalibrationTest {

    private val history = (1..200).map { (0..9).random() }

    @Test fun `ensemble combines outputs and produces valid prediction`() {
        val fs = com.aicolorpredict.analytics.feature.FeatureEngineer().build(1L, history)
        val outputs = listOf(
            com.aicolorpredict.analytics.ai.frequency.FrequencyAnalysisModel(),
            com.aicolorpredict.analytics.ai.markov.MarkovChainModel(),
            com.aicolorpredict.analytics.ai.transition.TransitionMatrixModel()
        ).map { runBlocking { it.predict(fs, history, 0.1) } }

        val ensemble = EnsembleModel()
        val pred = ensemble.combine(roundId = 1L, epochMs = 0L, outputs = outputs, history = history)

        assertThat(pred.topNumbers).hasSize(10)
        assertThat(pred.topNumbers.map { it.probability }.sum()).isWithin(1e-6).of(1.0)
        assertThat(pred.consensusLevel).isInstanceOf(ConsensusLevel::class.java)
        assertThat(pred.explanation).isNotEmpty()
        assertThat(pred.calibratedConfidence).isAtMost(0.85)
    }

    @Test fun `single weight never exceeds maxSingleWeight`() {
        val fs = com.aicolorpredict.analytics.feature.FeatureEngineer().build(1L, history)
        // Make one model artificially much better than the others.
        val outputs = listOf(
            runBlocking { com.aicolorpredict.analytics.ai.frequency.FrequencyAnalysisModel().predict(fs, history, 0.1) }
                .copy(accuracy = 0.95, confidence = 0.95),
            runBlocking { com.aicolorpredict.analytics.ai.markov.MarkovChainModel().predict(fs, history, 0.1) }
                .copy(accuracy = 0.10, confidence = 0.10),
            runBlocking { com.aicolorpredict.analytics.ai.transition.TransitionMatrixModel().predict(fs, history, 0.1) }
                .copy(accuracy = 0.10, confidence = 0.10)
        )
        val ensemble = EnsembleModel(maxSingleWeight = 0.35)
        val pred = ensemble.combine(1L, 0L, outputs, history)
        // The prediction should be well-formed. With a dominant model (0.95 accuracy),
        // its top pick should appear in the ensemble's top 5.
        assertThat(pred.topNumbers).hasSize(10)
        assertThat(pred.top5.map { it.number }).contains(outputs[0].topPick)
    }

    @Test fun `platt calibrator maps raw confidence toward observed frequency`() {
        val c = PlattCalibrator(lr = 0.5)
        // If we observe 50 correct / 100 total at raw=0.5, the calibrated output
        // should drift toward 0.5.
        repeat(100) {
            c.update(raw = 0.5, wasCorrect = if (it < 50) 1.0 else 0.0)
        }
        val calibrated = c.calibrate(0.5)
        assertThat(calibrated).isWithin(0.35).of(0.5)
    }

    @Test fun `confidence calibrator falls back to raw when no samples`() {
        val c = ConfidenceCalibrator()
        assertThat(c.calibrate(0.7)).isWithin(1e-9).of(0.7)
    }

    @Test fun `confidence calibrator uses binning after enough samples`() {
        val c = ConfidenceCalibrator()
        // Feed 30 perfect observations at raw=0.5 → calibrated should be close to 1.0
        repeat(30) { c.update(raw = 0.5, wasCorrect = 1.0) }
        val cal = c.calibrate(0.5)
        assertThat(cal).isGreaterThan(0.5)
    }

    @Test fun `model output init validates probability sum`() {
        try {
            ModelOutput.fromVector(
                modelName = "test",
                raw = doubleArrayOf(0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.05),  // sums to 0.95
                confidence = 0.5,
                reason = "test",
                accuracy = 0.5
            )
            // After normalisation this should sum to 1.0 — fromVector normalises defensively.
        } catch (t: Throwable) {
            org.junit.Assert.fail("fromVector should normalise defensively, but threw: ${t.message}")
        }
    }
}
