package com.aicolorpredict.analytics.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aicolorpredict.analytics.ui.theme.GreenPrimary
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed
import com.aicolorpredict.analytics.ui.theme.NumberViolet
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entriesOf

/**
 * Thin wrapper around Vico for the few chart shapes we use across the app.
 * Keeping this in one file means we can swap charting libraries without
 * touching every screen.
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
        val entries = remember(values) {
            values.mapIndexed { i, (_, v) ->
                com.patrykandpatrick.vico.core.entry.simple.simpleFloatEntry(i.toFloat(), v)
            }
        }
        val producer = remember(entries) { ChartEntryModelProducer(entries) }
        Chart(
            chart = columnChart(
                columns = arrayOf(
                    LineComponent(
                        shape = Shape.rectangle,
                        shader = DynamicShaders.fromBrush(
                            androidx.compose.ui.graphics.Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.55f)))
                        ),
                        thickness = 96f
                    )
                )
            ),
            chartModelProducer = producer,
            bottomAxis = rememberBottomAxis(),
            startAxis = rememberStartAxis(),
            modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 8.dp)
        )
    }
}

@Composable
fun LineChartCard(
    title: String,
    series: List<Pair<String, List<Float>>>,
    colors: List<Color> = listOf(GreenPrimary, NumberRed, NumberViolet, NumberGreen),
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val lines = series.mapIndexed { i, (_, ys) ->
            val color = colors.getOrNull(i % colors.size)
            lineSpec(
                lineShader = DynamicShaders.fromBrush(
                    androidx.compose.ui.graphics.Brush.verticalGradient(listOf(color ?: GreenPrimary, (color ?: GreenPrimary).copy(alpha = 0.3f)))
                )
            )
        }
        val producer = remember(series) {
            val listOfSeries = series.map { (_, ys) -> entriesOf(*ys.toTypedArray()) }
            ChartEntryModelProducer(listOfSeries)
        }
        Chart(
            chart = lineChart(lines = lines),
            chartModelProducer = producer,
            bottomAxis = rememberBottomAxis(),
            startAxis = rememberStartAxis(),
            modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 8.dp)
        )
    }
}
