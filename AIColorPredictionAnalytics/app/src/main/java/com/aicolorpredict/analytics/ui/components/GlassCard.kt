package com.aicolorpredict.analytics.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism surface — the primary container across the app.
 *
 * Vertical gradient + 1dp hairline border drawn directly on the content
 * via [Modifier.border]. This is critical: a previous implementation used a
 * second `Surface` overlay with `matchParentSize()` to draw the border, but
 * that overlay intercepted ALL touch events, making every button inside every
 * GlassCard non-responsive. Drawing the border inline on the content Box
 * eliminates the touch-blocking overlay entirely.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderAlpha: Float = 0.10f,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = borderAlpha)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor.copy(alpha = 0.78f),
                        surfaceVariant.copy(alpha = 0.55f)
                    )
                )
            )
            .border(1.dp, borderColor, shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}
