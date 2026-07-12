package com.aicolorpredict.analytics.ui.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.ui.components.GlassCard
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed
import com.aicolorpredict.analytics.util.DateUtils

@Composable
fun HistoryScreen(vm: HistoryViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            if (state.totalRounds > state.rounds.size) "Showing ${state.rounds.size} of ${state.totalRounds}"
            else "${state.totalRounds} rounds",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        if (state.rounds.isEmpty()) {
            Text("No rounds yet. Go to Enter to log one.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.rounds, key = { it.id }) { r ->
                    HistoryCard(r)
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(round: com.aicolorpredict.analytics.domain.model.ColorRound) {
    val color = if (round.color == AppColor.RED) NumberRed else NumberGreen
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(round.color.display.take(1), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(DateUtils.display(round.timestamp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Streak: ${round.streak} • Previous: ${round.previousColor?.display ?: "—"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("#${round.sequenceIndex}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
