package com.aicolorpredict.analytics.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.canvas.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.Pill
import com.aicolorpredict.analytics.ui.components.ProbabilityBar
import com.aicolorpredict.analytics.ui.components.numberColor
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed
import com.aicolorpredict.analytics.ui.theme.NumberViolet

@Composable
fun AnalyticsScreen(
    vm: AnalyticsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Honest statistics from your history.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // System metrics
        GlassCard {
            Text("System Accuracy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Top-1 ${"%.1f".format(state.systemMetrics.top1Accuracy * 100)}%", NumberGreen)
                Pill("Top-3 ${"%.1f".format(state.systemMetrics.top3Accuracy * 100)}%", NumberGreen)
                Pill("Top-5 ${"%.1f".format(state.systemMetrics.top5Accuracy * 100)}%", NumberGreen)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Rolling ${"%.1f".format(state.systemMetrics.rollingAccuracy * 100)}%", NumberViolet)
                Pill("F1 ${"%.1f".format(state.systemMetrics.macroF1 * 100)}%", NumberRed)
            }
        }

        // Frequency chart
        if (state.numberFrequency.isNotEmpty()) {
            GlassCard {
                Text("Number Frequency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                FrequencyChart(state.numberFrequency)
            }
        }

        // Hot / Cold
        GlassCard {
            Text("Hot & Cold Numbers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Hot", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.hotNumbers.forEach { n ->
                            NumberChip(n)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cold", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.coldNumbers.forEach { n ->
                            NumberChip(n, muted = true)
                        }
                    }
                }
            }
        }

        // Color distribution
        GlassCard {
            Text("Color Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            ColorDistribution(state.numberFrequency)
        }

        // Transition matrix heatmap
        GlassCard {
            Text("Transition Heatmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("P(next | previous) — tap a row to inspect", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            TransitionHeatmap(state.transitionStats, state.transitionFrom, vm::setTransitionFrom)
        }

        // Transition details
        state.transitionStats?.let { ts ->
            GlassCard {
                Text("After number ${ts.fromNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${ts.totalTransitions} observed transitions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                ts.nextNumberProbabilities.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .forEachIndexed { i, (n, p) ->
                        ProbabilityBar(rank = i + 1, number = n, probability = p)
                        Spacer(Modifier.height(4.dp))
                    }
            }
        }
    }
}

@Composable
private fun FrequencyChart(freq: Map<Int, Int>) {
    val maxVal = (freq.values.maxOrNull() ?: 1).toFloat().coerceAtLeast(1f)
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas
        val slot = w / 10f
        val barW = slot * 0.6f
        val gap = (slot - barW) / 2f
        val baseY = h * 0.95f
        drawLine(axisColor, Offset(0f, baseY), Offset(w, baseY), strokeWidth = 1f)
        (0..9).forEach { n ->
            val v = (freq[n] ?: 0).toFloat()
            val barH = (v / maxVal) * (h * 0.85f)
            val left = n * slot + gap
            val top = baseY - barH
            drawRoundRect(
                color = numberColor(n),
                topLeft = Offset(left, top),
                size = Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        (0..9).forEach {
            Text(it.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun NumberChip(n: Int, muted: Boolean = false) {
    val c = numberColor(n)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (muted) c.copy(alpha = 0.15f) else c.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Text(n.toString(), fontWeight = FontWeight.Bold, color = c)
    }
}

@Composable
private fun ColorDistribution(freq: Map<Int, Int>) {
    val total = freq.values.sum().coerceAtLeast(1)
    var red = 0; var green = 0; var violet = 0
    freq.forEach { (n, c) ->
        val colors = com.aicolorpredict.analytics.domain.model.colorsForNumber(n)
        if (colors.contains(BallColor.RED)) red += c
        if (colors.contains(BallColor.GREEN)) green += c
        if (colors.contains(BallColor.VIOLET)) violet += c
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ColorBar("Red", red.toDouble() / total, NumberRed)
        ColorBar("Green", green.toDouble() / total, NumberGreen)
        ColorBar("Violet", violet.toDouble() / total, NumberViolet)
    }
}

@Composable
private fun ColorBar(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${"%.1f".format(value * 100)}%", style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TransitionHeatmap(
    stats: com.aicolorpredict.analytics.domain.model.TransitionStats?,
    selectedFrom: Int,
    onSelect: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (0..9).forEach { n ->
            FilterChip(
                selected = selectedFrom == n,
                onClick = { onSelect(n) },
                label = { Text(n.toString()) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
