package com.aicolorpredict.analytics.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.components.numberColor
import com.aicolorpredict.analytics.ui.theme.ErrorRed
import com.aicolorpredict.analytics.ui.theme.SuccessGreen
import com.aicolorpredict.analytics.util.DateUtils

@Composable
fun HistoryScreen(
    vm: HistoryViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            if (state.totalRounds > state.rounds.size)
                "Showing ${state.rounds.size} of ${state.totalRounds} rounds — tap a card for details"
            else
                "${state.totalRounds} rounds — tap a card for details",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        if (state.rounds.isEmpty()) {
            Text(
                "No rounds yet. Go to the Enter tab to log one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.rounds, key = { it.id }) { r ->
                    HistoryCard(
                        round = r,
                        isExpanded = state.expandedRoundId == r.id,
                        predictions = if (state.expandedRoundId == r.id) state.predictionsForExpanded else emptyList(),
                        onClick = { vm.toggleExpand(r.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    round: Round,
    isExpanded: Boolean,
    predictions: List<com.aicolorpredict.analytics.domain.model.ModelOutput>,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: number badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Text(
                    round.number.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = numberColor(round.number)
                )
            }
            Spacer(Modifier.width(12.dp))
            // Middle: date/time + colors
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    DateUtils.display(round.epochMs),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    round.colors.joinToString(" · ") { it.display },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (round.streak > 1) {
                    Text(
                        "Streak ×${round.streak}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Right: prediction status
            PredictionStatusBadge(predictions, round.number)
        }

        // Expanded details
        AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                if (predictions.isEmpty()) {
                    Text("No prediction stored for this round.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Model predictions (made before this round):", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    predictions.forEach { p ->
                        val correct = p.topPick == round.number
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(p.modelName, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "top=${p.topPick} (${"%.1f".format(p.topProbability * 100)}%) ${if (correct) "✓" else "✗"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (correct) SuccessGreen else ErrorRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionStatusBadge(
    predictions: List<com.aicolorpredict.analytics.domain.model.ModelOutput>,
    actualNumber: Int
) {
    if (predictions.isEmpty()) {
        Text("—", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        val correctCount = predictions.count { it.topPick == actualNumber }
        val total = predictions.size
        val color = if (correctCount > total / 2) SuccessGreen else if (correctCount > 0) Color(0xFFFFA726) else ErrorRed
        Text(
            "$correctCount/$total",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
