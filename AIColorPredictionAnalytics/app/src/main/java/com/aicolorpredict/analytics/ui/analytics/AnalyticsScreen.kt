package com.aicolorpredict.analytics.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        // Color distribution
        GlassCard {
            Text("Color Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            val total = (state.redCount + state.greenCount).coerceAtLeast(1)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ColorStat("RED", state.redCount, state.redCount.toDouble() / total, NumberRed)
                ColorStat("GREEN", state.greenCount, state.greenCount.toDouble() / total, NumberGreen)
            }
        }

        // Transition matrix
        GlassCard {
            Text("Transition Matrix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("P(next | previous)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            val totalAfterRed = (state.redAfterRed + state.greenAfterRed).coerceAtLeast(1)
            val totalAfterGreen = (state.redAfterGreen + state.greenAfterGreen).coerceAtLeast(1)
            TransitionRow("After RED", state.redAfterRed, state.greenAfterRed, totalAfterRed)
            TransitionRow("After GREEN", state.redAfterGreen, state.greenAfterGreen, totalAfterGreen)
        }

        // Streak analysis
        GlassCard {
            Text("Streak Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Current: ${state.currentStreak}× ${state.currentStreakColor.display}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Longest RED: ${state.longestRedStreak}", style = MaterialTheme.typography.bodySmall, color = NumberRed)
                Text("Longest GREEN: ${state.longestGreenStreak}", style = MaterialTheme.typography.bodySmall, color = NumberGreen)
            }
        }

        // Recent trend
        GlassCard {
            Text("Recent Trend (last 50)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            TrendChart(state.recentRedPct)
        }
    }
}

@Composable
private fun ColorStat(label: String, count: Int, pct: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${"%.1f".format(pct * 100)}%", style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(color))
        Spacer(Modifier.height(2.dp))
        Text("$label ($count)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TransitionRow(label: String, redN: Int, greenN: Int, total: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text("R ${"%.0f".format(redN.toDouble() / total * 100)}%", style = MaterialTheme.typography.labelMedium, color = NumberRed, modifier = Modifier.weight(0.8f))
        Text("G ${"%.0f".format(greenN.toDouble() / total * 100)}%", style = MaterialTheme.typography.labelMedium, color = NumberGreen, modifier = Modifier.weight(0.8f))
    }
}

@Composable
private fun TrendChart(redPct: Double) {
    Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
        val w = size.width; val h = size.height
        val redW = (redPct * w).toFloat()
        drawRoundRect(NumberRed, Offset(0f, 0f), Size(redW, h.toFloat()), androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        drawRoundRect(NumberGreen, Offset(redW, 0f), Size(w - redW, h.toFloat()), androidx.compose.ui.geometry.CornerRadius(8f, 8f))
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${"%.0f".format(redPct * 100)}% RED", style = MaterialTheme.typography.labelSmall, color = NumberRed)
        Text("${"%.0f".format((1 - redPct) * 100)}% GREEN", style = MaterialTheme.typography.labelSmall, color = NumberGreen)
    }
}
