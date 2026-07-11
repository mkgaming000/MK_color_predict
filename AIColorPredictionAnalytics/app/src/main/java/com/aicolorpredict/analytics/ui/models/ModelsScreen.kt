package com.aicolorpredict.analytics.ui.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.ai.base.ModelCategory
import com.aicolorpredict.analytics.ui.components.EmptyState
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.StatChip

@Composable
fun ModelsScreen(
    vm: ModelsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Models", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Text("${state.registeredModels.size} models registered across ${ModelCategory.entries.size} categories.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))

        if (state.performance.isEmpty()) {
            EmptyState(title = "No performance data yet", subtitle = "Run a few predictions and add the outcomes to populate this screen.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.performance, key = { it.modelName }) { p ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(p.modelName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${p.samplesObserved} resolved samples", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatChip("Top-1", (p.top1Accuracy * 100).format(1) + "%")
                            StatChip("Top-3", (p.top3Accuracy * 100).format(1) + "%")
                            StatChip("Top-5", (p.top5Accuracy * 100).format(1) + "%")
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatChip("LogLoss", if (p.logLoss.isNaN()) "—" else p.logLoss.format(3))
                            StatChip("Brier", if (p.brierScore.isNaN()) "—" else p.brierScore.format(3))
                            StatChip("Rolling", (p.rollingAccuracy * 100).format(1) + "%")
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatChip("Precision", (p.precision * 100).format(1) + "%")
                            StatChip("Recall", (p.recall * 100).format(1) + "%")
                            StatChip("F1", (p.f1 * 100).format(1) + "%")
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format(d: Int): String = "%.${d}f".format(this)
