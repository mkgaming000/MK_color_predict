package com.aicolorpredict.analytics.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism surface used as the primary container across the app.
 *
 * The "glass" effect is a subtle vertical gradient over a translucent
 * background, plus a 1dp hairline border at low opacity. We deliberately avoid
 * a real blur backdrop (RenderEffect.createBlurEffect) because it's API 31+;
 * on older devices the gradient + border alone reads as "frosted" enough.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderAlpha: Float = 0.12f,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = borderAlpha)

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp)),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                surfaceColor.copy(alpha = 0.72f),
                                surfaceVariant.copy(alpha = 0.55f)
                            )
                        )
                    )
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
        // Hairline border overlay — matchParentSize so it covers the gradient
        // Surface below. Without matchParentSize the empty-content Surface would
        // collapse to 0×0 and the border would never render.
        Surface(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(22.dp)),
            color = Color.Transparent,
            border = BorderStroke(1.dp, borderColor)
        ) {}
    }
}
