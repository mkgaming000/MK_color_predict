package com.aicolorpredict.analytics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Color_White = Color.White

private val DarkColors = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = DarkBackground,
    primaryContainer = GreenDark,
    onPrimaryContainer = Color_White,
    secondary = VioletAccent,
    onSecondary = Color_White,
    tertiary = GoldAccent,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = RedAccent
)

private val LightColors = lightColorScheme(
    primary = GreenDark,
    onPrimary = Color_White,
    primaryContainer = GreenPrimary,
    onPrimaryContainer = LightBackground,
    secondary = VioletAccent,
    onSecondary = Color_White,
    tertiary = GoldAccent,
    onTertiary = LightBackground,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = RedAccent
)

@Composable
fun AICpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = AICpTypography,
        shapes = AICpShapes,
        content = content
    )
}
