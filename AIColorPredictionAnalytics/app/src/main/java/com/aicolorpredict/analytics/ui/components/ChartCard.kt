package com.aicolorpredict.analytics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aicolorpredict.analytics.ui.theme.GreenPrimary

/**
 * Pure-Compose-Canvas bar chart — zero external API risk.
 *
 * Draws vertical bars for each (label, value) pair. The tallest bar fills the
 * available height; others scale proportionally. A subtle vertical gradient
 * gives each bar a "frosted" look matching the glassmorphism theme.
 */
@Composable
fun BarChartCard(
    title: String,
    values: List<Pair<String, Float>>,
    barColor: Color = GreenPrimary,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val maxVal = remember(values) { values.maxOfOrNull { it.second }?.coerceAtLeast(0.001f) ?: 0.001f }
        val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        val textColor = MaterialTheme.colorScheme.onSurfaceVariant
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp)
        ) {
            val canvasW = size.width
            val canvasH = size.height
            if (canvasW <= 0f || canvasH <= 0f || values.isEmpty()) return@Canvas
            val barCount = values.size
            val slotWidth = canvasW / barCount
            val barWidth = slotWidth * 0.6f
            val barGap = (slotWidth - barWidth) / 2f
            val baseY = canvasH * 0.95f
            // Baseline
            drawLine(
                color = axisColor,
                start = Offset(0f, baseY),
                end = Offset(canvasW, baseY),
                strokeWidth = 1f
            )
            values.forEachIndexed { i, (_, v) ->
                val barH = (v / maxVal) * (canvasH * 0.85f)
                val left = i * slotWidth + barGap
                val top = baseY - barH
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(barColor, barColor.copy(alpha = 0.55f)),
                        startY = top,
                        endY = baseY
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }
        }
        // X-axis labels
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            values.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Pure-Compose-Canvas line chart — supports multiple series.
 *
 * Each series is drawn as a smooth stroked path. Series colors cycle through
 * the provided palette. The chart auto-scales to the min/max of all series.
 */
@Composable
fun LineChartCard(
    title: String,
    series: List<Pair<String, List<Float>>>,
    colors: List<Color> = listOf(GreenPrimary, com.aicolorpredict.analytics.ui.theme.NumberRed, com.aicolorpredict.analytics.ui.theme.NumberViolet),
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val allValues = remember(series) { series.flatMap { it.second } }
        val maxVal = remember(allValues) { allValues.maxOrNull() ?: 1f }
        val minVal = remember(allValues) { allValues.minOrNull() ?: 0f }
        val range = (maxVal - minVal).coerceAtLeast(0.001f)
        val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp)
        ) {
            val canvasW = size.width
            val canvasH = size.height
            if (canvasW <= 0f || canvasH <= 0f || series.isEmpty()) return@Canvas
            val baseY = canvasH * 0.95f
            val topY = canvasH * 0.05f
            val plotH = baseY - topY
            // Baseline
            drawLine(
                color = axisColor,
                start = Offset(0f, baseY),
                end = Offset(canvasW, baseY),
                strokeWidth = 1f
            )
            series.forEachIndexed { si, (_, ys) ->
                if (ys.isEmpty()) return@forEachIndexed
                val color = colors.getOrElse(si % colors.size) { GreenPrimary }
                val stepX = if (ys.size > 1) canvasW / (ys.size - 1) else canvasW
                val path = Path()
                ys.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = baseY - ((v - minVal) / range) * plotH
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}
