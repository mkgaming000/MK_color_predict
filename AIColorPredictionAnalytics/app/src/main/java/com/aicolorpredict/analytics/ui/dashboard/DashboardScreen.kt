package com.aicolorpredict.analytics.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.Confidence
import com.aicolorpredict.analytics.domain.model.Prediction
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.Pill
import com.aicolorpredict.analytics.ui.components.ProbabilityBar
import com.aicolorpredict.analytics.ui.components.numberColor
import com.aicolorpredict.analytics.ui.theme.ErrorRed
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed
import com.aicolorpredict.analytics.ui.theme.NumberViolet
import com.aicolorpredict.analytics.ui.theme.SuccessGreen
import com.aicolorpredict.analytics.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    vm: DashboardViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.refreshPrediction() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh prediction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "MK Color Predict",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Statistical estimates from historical data — not a guarantee of future outcomes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Loading overlay
            AnimatedVisibility(visible = state.isLoading, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Text("Computing prediction…", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Error
            state.errorMessage?.let {
                GlassCard { Text("Error: $it", color = ErrorRed) }
            }

            // Current Prediction card
            CurrentPredictionCard(state.latestPrediction, state.totalRounds)

            // Last entered number + status
            LastEnteredCard(state.recentRounds)

            // Recent history (compact)
            RecentHistoryCard(state.recentRounds)

            Spacer(Modifier.height(80.dp)) // FAB clearance
        }
    }
}

@Composable
private fun CurrentPredictionCard(prediction: Prediction?, totalRounds: Int) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Current Prediction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Based on $totalRounds rounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            prediction?.let { Pill(it.consensusLevel.label, consensusColor(it)) }
        }
        Spacer(Modifier.height(12.dp))

        if (prediction == null) {
            Text(
                "Tap the refresh button to generate a prediction.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Top 5 numbers
            Text(
                "Top 5 Numbers",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            prediction.top5.forEachIndexed { i, np ->
                ProbabilityBar(rank = i + 1, number = np.number, probability = np.probability)
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(12.dp))

            // Confidence + color split
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Pill(
                    "Confidence ${"%.0f".format(prediction.calibratedConfidence * 100)}%",
                    confidenceColor(prediction)
                )
                ColorSplitRow(prediction)
            }
        }
    }
}

@Composable
private fun ColorSplitRow(prediction: Prediction) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        ColorChip("R", NumberRed, prediction.colorProbabilities[BallColor.RED]!!)
        ColorChip("G", NumberGreen, prediction.colorProbabilities[BallColor.GREEN]!!)
        ColorChip("V", NumberViolet, prediction.colorProbabilities[BallColor.VIOLET]!!)
    }
}

@Composable
private fun ColorChip(label: String, color: Color, value: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(
            "$label ${"%.0f".format(value * 100)}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LastEnteredCard(recentRounds: List<com.aicolorpredict.analytics.domain.model.Round>) {
    val last = recentRounds.firstOrNull()
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Last Entered", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (last == null) {
                    Text("No rounds yet", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        "Number ${last.number}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = numberColor(last.number)
                    )
                    Text(
                        "${last.colors.joinToString(" · ") { it.display }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (last != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(numberColor(last.number).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        last.number.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = numberColor(last.number)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentHistoryCard(recentRounds: List<com.aicolorpredict.analytics.domain.model.Round>) {
    GlassCard {
        Text("Recent History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (recentRounds.isEmpty()) {
            Text("No rounds yet — go to the Enter tab to log one.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            recentRounds.take(6).forEach { r ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#${r.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(numberColor(r.number).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(r.number.toString(), fontWeight = FontWeight.Bold, color = numberColor(r.number), fontSize = 14.sp)
                    }
                    Text(r.colors.joinToString(" · ") { it.display }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun consensusColor(p: Prediction): Color = when (p.consensusLevel) {
    com.aicolorpredict.analytics.domain.model.ConsensusLevel.STRONG -> SuccessGreen
    com.aicolorpredict.analytics.domain.model.ConsensusLevel.MODERATE -> WarningAmber
    com.aicolorpredict.analytics.domain.model.ConsensusLevel.WEAK -> ErrorRed
}

private fun confidenceColor(p: Prediction): Color = when {
    p.calibratedConfidence >= 0.5 -> SuccessGreen
    p.calibratedConfidence >= 0.25 -> WarningAmber
    else -> ErrorRed
}
