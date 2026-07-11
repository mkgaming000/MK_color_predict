package com.aicolorpredict.analytics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aicolorpredict.analytics.domain.model.Confidence
import com.aicolorpredict.analytics.domain.model.ConsensusLevel
import com.aicolorpredict.analytics.ui.theme.GoldAccent
import com.aicolorpredict.analytics.ui.theme.GreenPrimary
import com.aicolorpredict.analytics.ui.theme.RedAccent
import com.aicolorpredict.analytics.ui.theme.VioletAccent

@Composable
fun ConfidenceBadge(
    confidence: Confidence,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (confidence) {
        Confidence.VERY_HIGH -> GreenPrimary to "Very High"
        Confidence.HIGH -> GreenPrimary to "High"
        Confidence.MEDIUM -> GoldAccent to "Medium"
        Confidence.LOW -> RedAccent to "Low"
    }
    Pill(color = color, label = "${confidence.label} confidence", modifier = modifier)
}

@Composable
fun ConsensusBadge(level: ConsensusLevel, modifier: Modifier = Modifier) {
    val color = when (level) {
        ConsensusLevel.STRONG -> GreenPrimary
        ConsensusLevel.MODERATE -> GoldAccent
        ConsensusLevel.WEAK -> RedAccent
    }
    Pill(color = color, label = "Consensus: ${level.label}", modifier = modifier)
}

@Composable
fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = VioletAccent
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label  $value",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Pill(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
