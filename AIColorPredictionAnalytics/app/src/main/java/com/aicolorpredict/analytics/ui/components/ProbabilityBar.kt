package com.aicolorpredict.analytics.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.aicolorpredict.analytics.domain.model.Confidence
import com.aicolorpredict.analytics.domain.model.colorsForNumber
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed

/**
 * A horizontal bar showing the probability of a single number.
 *
 * Layout:
 *   [ rank ] [ number chip ] [ ............ bar ............ ] [ prob% ]
 *
 * The bar animates from 0 → target width on first composition (and on every
 * value change) using a 600ms ease tween.
 */
@Composable
fun ProbabilityBar(
    rank: Int,
    number: Int,
    probability: Double,
    confidence: Confidence = Confidence.LOW,
    modifier: Modifier = Modifier
) {
    val colors = colorsForNumber(number)
    val barColor = when {
        colors.contains(com.aicolorpredict.analytics.domain.model.BallColor.GREEN) -> NumberGreen
        colors.contains(com.aicolorpredict.analytics.domain.model.BallColor.RED) -> NumberRed
        else -> MaterialTheme.colorScheme.primary
    }

    val animated by animateFloatAsState(
        targetValue = probability.toFloat(),
        animationSpec = tween(600),
        label = "prob-bar"
    )

    Row(
        modifier = modifier.fillMaxWidth().height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp)
        )
        NumberChip(number = number, color = barColor)
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(0.55f)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${(probability * 100).format(1)}%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NumberChip(number: Int, color: Color) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun Double.format(d: Int): String = "%.${d}f".format(this)
