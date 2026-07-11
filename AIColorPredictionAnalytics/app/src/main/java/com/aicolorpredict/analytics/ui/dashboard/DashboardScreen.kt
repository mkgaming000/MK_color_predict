package com.aicolorpredict.analytics.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.ui.components.ConfidenceBadge
import com.aicolorpredict.analytics.ui.components.ConsensusBadge
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.ProbabilityBar
import com.aicolorpredict.analytics.ui.components.StatChip

@Composable
fun DashboardScreen(
    vm: DashboardViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "AI Color Prediction Analytics",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Statistical estimates from historical data. Not a guarantee of future outcomes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Top stats row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("Total rounds", state.totalRounds.toString())
            StatChip("Top-1 acc", "${(state.systemMetrics.top1Accuracy * 100).format(1)}%")
            StatChip("Top-3 acc", "${(state.systemMetrics.top3Accuracy * 100).format(1)}%")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("LogLoss", if (state.systemMetrics.logLoss.isNaN()) "—" else state.systemMetrics.logLoss.format(3))
            StatChip("Brier", if (state.systemMetrics.brierScore.isNaN()) "—" else state.systemMetrics.brierScore.format(3))
            StatChip("F1", (state.systemMetrics.macroF1 * 100).format(1) + "%")
        }

        Spacer(Modifier.height(8.dp))

        // Refresh button
        Button(
            onClick = { vm.refreshPrediction() },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.width(20.dp).height(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text("Run AI Prediction")
        }

        // Latest prediction
        val pred = state.latestPrediction
        if (pred == null) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tap “Run AI Prediction” to compute the next-round estimate from the current history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            PredictionSummaryCard(state)
        }

        // Recent rounds preview
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Recent rounds",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            if (state.recentRounds.isEmpty()) {
                Text(
                    text = "No rounds yet — add one from the Data tab.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.recentRounds.take(8).forEach { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("#${r.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Number ${r.number}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(r.colors.joinToString(",") { it.display }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PredictionSummaryCard(state: DashboardUiState) {
    val pred = state.latestPrediction ?: return
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top 10 Number Probabilities",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            ConsensusBadge(level = pred.consensusLevel)
        }
        Spacer(Modifier.height(8.dp))
        pred.top10.forEachIndexed { i, np ->
            ProbabilityBar(rank = i + 1, number = np.number, probability = np.probability, confidence = np.confidence)
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ConfidenceBadge(confidence = com.aicolorpredict.analytics.domain.model.Confidence.fromProbability(pred.top1.probability, pred.consensusLevel))
            StatChip("Calibrated conf.", (pred.calibratedConfidence * 100).format(1) + "%")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Color split — Red ${(pred.colorProbabilities[com.aicolorpredict.analytics.domain.model.BallColor.RED]!! * 100).format(1)}%  •  Green ${(pred.colorProbabilities[com.aicolorpredict.analytics.domain.model.BallColor.GREEN]!! * 100).format(1)}%  •  Violet ${(pred.colorProbabilities[com.aicolorpredict.analytics.domain.model.BallColor.VIOLET]!! * 100).format(1)}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Why this estimate?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = pred.explanation,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Double.format(d: Int): String = "%.${d}f".format(this)
