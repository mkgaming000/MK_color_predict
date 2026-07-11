package com.aicolorpredict.analytics.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.ui.components.BarChartCard
import com.aicolorpredict.analytics.ui.components.EmptyState
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.StatChip

@Composable
fun AnalyticsScreen(
    vm: AnalyticsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Text("Honest, real-time metrics computed from resolved predictions only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // System-level metrics
        GlassCard {
            Text("System Accuracy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Top-1", (state.systemMetrics.top1Accuracy * 100).format(1) + "%")
                StatChip("Top-3", (state.systemMetrics.top3Accuracy * 100).format(1) + "%")
                StatChip("Top-5", (state.systemMetrics.top5Accuracy * 100).format(1) + "%")
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("LogLoss", if (state.systemMetrics.logLoss.isNaN()) "—" else state.systemMetrics.logLoss.format(3))
                StatChip("Brier", if (state.systemMetrics.brierScore.isNaN()) "—" else state.systemMetrics.brierScore.format(3))
                StatChip("F1", (state.systemMetrics.macroF1 * 100).format(1) + "%")
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Precision", (state.systemMetrics.macroPrecision * 100).format(1) + "%")
                StatChip("Recall", (state.systemMetrics.macroRecall * 100).format(1) + "%")
                StatChip("Rolling", (state.systemMetrics.rollingAccuracy * 100).format(1) + "%")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Based on ${state.systemMetrics.totalPredictions} resolved predictions across all models.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Number histogram
        if (state.numberHistogram.isNotEmpty()) {
            BarChartCard(
                title = "Number Frequency",
                values = (0..9).map { it.toString() to (state.numberHistogram[it]?.toFloat() ?: 0f) }
            )
        }

        // Per-model table
        GlassCard {
            Text("Per-Model Comparison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (state.perModel.isEmpty()) {
                Text("No resolved predictions yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                // Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Model", modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Top1", modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Top3", modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Top5", modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("F1", modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                state.perModel.forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.modelName, modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.bodySmall)
                        Text((p.top1Accuracy * 100).format(1) + "%", modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.labelMedium)
                        Text((p.top3Accuracy * 100).format(1) + "%", modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.labelMedium)
                        Text((p.top5Accuracy * 100).format(1) + "%", modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.labelMedium)
                        Text((p.f1 * 100).format(1) + "%", modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            GlassCard { Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        }
    }
}

private fun Double.format(d: Int): String = "%.${d}f".format(this)
