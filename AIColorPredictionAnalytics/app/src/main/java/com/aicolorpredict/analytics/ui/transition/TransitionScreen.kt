package com.aicolorpredict.analytics.ui.transition

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.ui.components.BarChartCard
import com.aicolorpredict.analytics.ui.components.EmptyState
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.ProbabilityBar
import com.aicolorpredict.analytics.ui.components.StatChip

@Composable
fun TransitionScreen(
    vm: TransitionViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Transition Analysis", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Text("Given previous number X, what comes next? All values are observed frequencies from history.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // From-number selector
        GlassCard {
            Text("Previous number", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0..9).forEach { n ->
                    FilterChip(
                        selected = state.fromNumber == n,
                        onClick = { vm.load(n) },
                        label = { Text(n.toString()) }
                    )
                }
            }
        }

        val stats = state.stats
        if (stats == null) {
            EmptyState(title = if (state.isLoading) "Loading…" else "No data", subtitle = state.errorMessage)
        } else {
            // Summary
            GlassCard {
                Text("Summary for previous = ${stats.fromNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip("Transitions", stats.totalTransitions.toString())
                    StatChip("Historical", stats.historicalCount.toString())
                }
            }

            // Next number probabilities as bars
            GlassCard {
                Text("Most common next numbers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                val sorted = stats.nextNumberProbabilities.entries.sortedByDescending { it.value }
                sorted.forEachIndexed { i, (n, p) ->
                    ProbabilityBar(rank = i + 1, number = n, probability = p)
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Next color probabilities
            GlassCard {
                Text("Next color probabilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip("Red", (stats.nextColorProbabilities[BallColor.RED]!! * 100).format(1) + "%", accent = com.aicolorpredict.analytics.ui.theme.NumberRed)
                    StatChip("Green", (stats.nextColorProbabilities[BallColor.GREEN]!! * 100).format(1) + "%", accent = com.aicolorpredict.analytics.ui.theme.NumberGreen)
                    StatChip("Violet", (stats.nextColorProbabilities[BallColor.VIOLET]!! * 100).format(1) + "%", accent = com.aicolorpredict.analytics.ui.theme.NumberViolet)
                }
            }

            // Average gaps
            GlassCard {
                Text("Average gap (rounds between repeats)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                (0..9).forEach { n ->
                    val g = stats.averageGap[n] ?: Double.NaN
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Number $n", style = MaterialTheme.typography.bodySmall)
                        Text(if (g.isNaN()) "—" else g.format(2), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private fun Double.format(d: Int): String = "%.${d}f".format(this)
