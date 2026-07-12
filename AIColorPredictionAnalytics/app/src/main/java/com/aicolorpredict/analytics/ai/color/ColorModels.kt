package com.aicolorpredict.analytics.ai.color

import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.domain.model.ColorModelOutput
import kotlin.math.exp
import kotlin.math.ln

// ============================================================
// 1. Frequency Model
// ============================================================
class FrequencyColorModel : ColorPredictionModel {
    override val name = "Frequency"
    override suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput {
        val rp = ColorStats.redFrequency(history)
        val conf = ColorStats.confidenceFromProb(rp) * 0.8
        val reason = "Overall frequency: ${"%.1f".format(rp * 100)}% RED, ${"%.1f".format((1 - rp) * 100)}% GREEN (${history.size} samples)."
        return ColorModelOutput.fromRedProb(name, rp, conf, reason, rollingAccuracy)
    }
}

// ============================================================
// 2. Markov Chain (1st-order transition)
// ============================================================
class MarkovColorModel : ColorPredictionModel {
    override val name = "Markov Chain"
    override suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput {
        if (history.size < 2) {
            return ColorModelOutput.fromRedProb(name, 0.5, 0.0, "Insufficient history for Markov.", rollingAccuracy)
        }
        val last = history.last()
        var redAfter = 0; var greenAfter = 0
        for (i in 1 until history.size) {
            if (history[i - 1] == last) {
                if (history[i] == AppColor.RED) redAfter++ else greenAfter++
            }
        }
        val total = redAfter + greenAfter
        // Laplace smoothing with alpha=1, denom = total + 2
        val rp = (redAfter + 1.0) / (total + 2.0)
        val sampleFactor = 1.0 - exp(-total / 10.0)
        val conf = ColorStats.confidenceFromProb(rp) * sampleFactor * 0.85
        val reason = "After ${last.display}: ${redAfter}R/${greenAfter}G observed ($total transitions). P(RED|${last.display})=${"%.1f".format(rp * 100)}%."
        return ColorModelOutput.fromRedProb(name, rp, conf, reason, rollingAccuracy)
    }
}

// ============================================================
// 3. Bayesian Model
// ============================================================
class BayesianColorModel : ColorPredictionModel {
    override val name = "Bayesian"
    override suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput {
        if (history.isEmpty()) {
            return ColorModelOutput.fromRedProb(name, 0.5, 0.0, "No history for Bayesian.", rollingAccuracy)
        }
        // Prior = overall frequency
        val prior = ColorStats.redFrequency(history)
        // Likelihood: P(RED | streak >= 3) is higher if current streak is RED
        val last = history.last()
        val streak = countStreak(history)
        val streakBoost = if (last == AppColor.RED) streak * 0.02 else -streak * 0.02
        val rp = (prior + streakBoost).coerceIn(0.05, 0.95)
        val conf = ColorStats.confidenceFromProb(rp) * 0.75
        val reason = "Prior=${"%.1f".format(prior * 100)}% RED, streak=$streak (${last.display}), posterior=${"%.1f".format(rp * 100)}% RED."
        return ColorModelOutput.fromRedProb(name, rp, conf, reason, rollingAccuracy)
    }
    private fun countStreak(history: List<AppColor>): Int {
        if (history.isEmpty()) return 0
        val last = history.last()
        var s = 1
        for (i in history.size - 2 downTo 0) {
            if (history[i] == last) s++ else break
        }
        return s
    }
}

// ============================================================
// 4. Moving Average (weighted recent frequency)
// ============================================================
class MovingAverageColorModel : ColorPredictionModel {
    override val name = "Moving Average"
    override suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput {
        if (history.isEmpty()) {
            return ColorModelOutput.fromRedProb(name, 0.5, 0.0, "No history.", rollingAccuracy)
        }
        val window = history.takeLast(minOf(history.size, 20))
        var wSum = 0.0; var wTotal = 0.0
        for (i in window.indices) {
            val w = (i + 1).toDouble()
            if (window[i] == AppColor.RED) wSum += w
            wTotal += w
        }
        val rp = if (wTotal > 0) wSum / wTotal else 0.5
        val conf = ColorStats.confidenceFromProb(rp) * 0.80
        val reason = "Weighted last ${window.size}: ${"%.1f".format(rp * 100)}% RED (recent rounds weighted higher)."
        return ColorModelOutput.fromRedProb(name, rp, conf, reason, rollingAccuracy)
    }
}

// ============================================================
// 5. Trend Detection
// ============================================================
class TrendColorModel : ColorPredictionModel {
    override val name = "Trend Detection"
    override suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput {
        if (history.size < 10) {
            return ColorModelOutput.fromRedProb(name, 0.5, 0.0, "Need >=10 rounds for trend.", rollingAccuracy)
        }
        val recent = history.takeLast(10)
        val older = history.dropLast(10).takeLast(10)
        val recentRed = recent.count { it == AppColor.RED }.toDouble() / recent.size
        val olderRed = older.count { it == AppColor.RED }.toDouble() / older.size.coerceAtLeast(1)
        val trend = recentRed - olderRed  // positive = RED increasing
        val rp = (0.5 + trend * 0.8).coerceIn(0.05, 0.95)
        val conf = ColorStats.confidenceFromProb(rp) * 0.70
        val dir = if (trend > 0.05) "RED rising" else if (trend < -0.05) "GREEN rising" else "stable"
        val reason = "Recent RED=${"%.0f".format(recentRed * 100)}% vs older=${"%.0f".format(olderRed * 100)}% → $dir."
        return ColorModelOutput.fromRedProb(name, rp, conf, reason, rollingAccuracy)
    }
}

// ============================================================
// 6. Transition Model (2nd-order)
// ============================================================
class TransitionColorModel : ColorPredictionModel {
    override val name = "Transition"
    override suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput {
        if (history.size < 3) {
            return ColorModelOutput.fromRedProb(name, 0.5, 0.0, "Need >=3 rounds for 2nd-order.", rollingAccuracy)
        }
        val s1 = history[history.size - 2]
        val s2 = history[history.size - 1]
        var redAfter = 0; var greenAfter = 0
        for (i in 2 until history.size) {
            if (history[i - 2] == s1 && history[i - 1] == s2) {
                if (history[i] == AppColor.RED) redAfter++ else greenAfter++
            }
        }
        val total = redAfter + greenAfter
        val rp = (redAfter + 1.0) / (total + 2.0)
        val conf = if (total >= 3) ColorStats.confidenceFromProb(rp) * 0.80 else 0.3
        val reason = "After ${s1.display}→${s2.display}: ${redAfter}R/${greenAfter}G ($total obs). P(RED)=${"%.1f".format(rp * 100)}%."
        return ColorModelOutput.fromRedProb(name, rp, conf, reason, rollingAccuracy)
    }
}

// ============================================================
// 7. Streak Model (momentum / mean reversion)
// ============================================================
class StreakColorModel : ColorPredictionModel {
    override val name = "Streak"
    override suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput {
        if (history.isEmpty()) {
            return ColorModelOutput.fromRedProb(name, 0.5, 0.0, "No history.", rollingAccuracy)
        }
        val last = history.last()
        var streak = 1
        for (i in history.size - 2 downTo 0) {
            if (history[i] == last) streak++ else break
        }
        // Mean reversion: longer streak → higher probability of reversal
        val reversalProb = (streak * 0.08).coerceAtMost(0.35)
        val rp = if (last == AppColor.RED) 1.0 - reversalProb else reversalProb
        val conf = (0.4 + streak * 0.03).coerceAtMost(0.75)
        val reason = "Current ${last.display} streak=$streak. Mean-reversion model gives ${"%.1f".format(rp * 100)}% RED."
        return ColorModelOutput.fromRedProb(name, rp, conf, reason, rollingAccuracy)
    }
}

// ============================================================
// 8. Alternation Model (detects R-G-R-G patterns)
// ============================================================
class AlternationColorModel : ColorPredictionModel {
    override val name = "Alternation"
    override suspend fun predict(history: List<AppColor>, rollingAccuracy: Double): ColorModelOutput {
        if (history.size < 4) {
            return ColorModelOutput.fromRedProb(name, 0.5, 0.0, "Need >=4 rounds.", rollingAccuracy)
        }
        val last4 = history.takeLast(4)
        var alternations = 0
        for (i in 1 until last4.size) {
            if (last4[i] != last4[i - 1]) alternations++
        }
        val isAlternating = alternations >= 3
        val last = history.last()
        val rp = if (isAlternating) {
            // If alternating, predict opposite of last
            if (last == AppColor.RED) 0.0 else 1.0
        } else {
            // Not alternating, weak signal
            if (last == AppColor.RED) 0.55 else 0.45
        }
        val conf = if (isAlternating) 0.65 else 0.35
        val reason = if (isAlternating) "Strong alternation pattern detected — predicting ${AppColor.opposite(last).display}." else "No clear alternation."
        return ColorModelOutput.fromRedProb(name, rp, conf, reason, rollingAccuracy)
    }
}
