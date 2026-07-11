package com.aicolorpredict.analytics.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aicolorpredict.analytics.domain.model.BallColor
import com.aicolorpredict.analytics.domain.model.colorsForNumber
import com.aicolorpredict.analytics.ui.theme.NumberGreen
import com.aicolorpredict.analytics.ui.theme.NumberRed
import com.aicolorpredict.analytics.ui.theme.NumberViolet

/** Returns the primary UI color for a number 0..9. */
fun numberColor(n: Int): Color = when (n % 2) {
    0 -> NumberRed
    else -> NumberGreen
}

/** Returns every color (including Violet) for a number, as a Compose color list. */
fun numberColors(n: Int): List<Color> = colorsForNumber(n).map { c ->
    when (c) {
        BallColor.RED -> NumberRed
        BallColor.GREEN -> NumberGreen
        BallColor.VIOLET -> NumberViolet
    }
}

/**
 * A circular number button used on the Enter Result screen.
 *
 * - Tap toggles selection.
 * - The button's background gradient is derived from the number's color(s).
 * - Violet-carrying numbers (0 and 5) get a violet ring so users can see at a
 *   glance which numbers are "double-color".
 */
@Composable
fun NumberButton(
    number: Int,
    selected: Boolean,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 64.dp
) {
    val colors = numberColors(number)
    val bg = if (colors.size == 1) colors[0] else
        Brush.linearGradient(colors).let { _ -> colors.first() }
    val ringColor = if (colors.contains(NumberViolet)) NumberViolet else colors[0]

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1.0f,
        animationSpec = tween(200),
        label = "number-btn-scale"
    )

    Box(
        modifier = modifier
            .size(size * scale)
            .clip(CircleShape)
            .background(
                if (selected) bg
                else bg.copy(alpha = 0.25f)
            )
            .clickable { onClick(number) },
        contentAlignment = Alignment.Center
    ) {
        // Violet ring for 0 and 5
        if (colors.contains(NumberViolet)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Color.Transparent)
            )
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else ringColor
            )
        } else {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else bg
            )
        }
    }
}

/**
 * Small color swatch row — shows the colors associated with a number.
 */
@Composable
fun ColorSwatchRow(
    number: Int,
    modifier: Modifier = Modifier,
    swatchSize: androidx.compose.ui.unit.Dp = 14.dp
) {
    val colors = numberColors(number)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        colors.forEach { c ->
            Box(
                modifier = Modifier
                    .size(swatchSize)
                    .clip(RoundedCornerShape(50))
                    .background(c)
            )
        }
    }
}

/**
 * A single horizontal probability bar with animated fill.
 */
@Composable
fun ProbabilityBar(
    rank: Int,
    number: Int,
    probability: Double,
    modifier: Modifier = Modifier
) {
    val barColor = numberColor(number)
    val animated by animateFloatAsState(
        targetValue = probability.toFloat(),
        animationSpec = tween(600),
        label = "prob-bar"
    )
    Row(
        modifier = modifier.fillMaxWidth().height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(barColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = barColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(0.4f)
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
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${"%.1f".format(probability * 100)}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Small pill badge — used for confidence / consensus / status labels.
 */
@Composable
fun Pill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
