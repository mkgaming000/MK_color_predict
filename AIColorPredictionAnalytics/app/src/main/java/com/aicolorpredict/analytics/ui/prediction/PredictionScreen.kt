package com.aicolorpredict.analytics.ui.prediction

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.Confidence
import com.aicolorpredict.analytics.ui.components.ConfidenceBadge
import com.aicolorpredict.analytics.ui.components.ConsensusBadge
import com.aicolorpredict.analytics.ui.components.EmptyState
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.ProbabilityBar
import com.aicolorpredict.analytics.ui.components.StatChip

@Composable
fun PredictionScreen(
    vm: PredictionViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { if (state.prediction == null && !state.isLoading) vm.predict() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Live Prediction",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Every probability below is a statistical estimate from historical data. Random games are unpredictable — these numbers are not guarantees.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = { vm.predict() },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text("Recompute Prediction")
        }

        val pred = state.prediction
        if (pred == null) {
            EmptyState(
                title = if (state.isLoading) "Computing…" else "No prediction yet",
                subtitle = if (state.errorMessage != null) state.errorMessage else "Tap the button above to run the AI pipeline."
            )
        } else {
            // Top-10 numbers
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Top 10 Number Probabilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    ConsensusBadge(level = pred.consensusLevel)
                }
                Spacer(Modifier.height(8.dp))
                pred.top10.forEachIndexed { i, np ->
                    ProbabilityBar(rank = i + 1, number = np.number, probability = np.probability, confidence = np.confidence)
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Color probabilities
            GlassCard {
                Text("Color Probabilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                ColorRow("Green", pred.colorProbabilities[BallColor.GREEN]!!, com.aicolorpredict.analytics.ui.theme.NumberGreen)
                ColorRow("Red", pred.colorProbabilities[BallColor.RED]!!, com.aicolorpredict.analytics.ui.theme.NumberRed)
                ColorRow("Violet", pred.colorProbabilities[BallColor.VIOLET]!!, com.aicolorpredict.analytics.ui.theme.NumberViolet)
            }

            // Confidence + explanation
            GlassCard {
                Text("Confidence & Explanation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConfidenceBadge(confidence = Confidence.fromProbability(pred.top1.probability, pred.consensusLevel))
                    StatChip("Calibrated", (pred.calibratedConfidence * 100).format(1) + "%")
                }
                Spacer(Modifier.height(12.dp))
                Text("Why this estimate?", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = pred.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Per-model breakdown
            GlassCard {
                Text("Per-Model Output", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                pred.modelOutputs.forEach { o ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(o.modelName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "top=${o.topPick} (${(o.topProbability * 100).format(1)}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorRow(label: String, value: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        Text("${(value * 100).format(1)}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun Double.format(d: Int): String = "%.${d}f".format(this)
