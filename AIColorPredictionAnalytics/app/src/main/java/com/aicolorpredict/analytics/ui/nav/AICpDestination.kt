package com.aicolorpredict.analytics.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom-navigation destinations.
 *
 * Deliberately limited to 5 tabs — the spec calls for minimal navigation.
 *   Dashboard  → current prediction + quick add + recent history
 *   Enter      → enter a new round (date/time/number/color)
 *   History    → timeline of past rounds with prediction-vs-actual
 *   Analytics  → frequency / heatmap / transitions / hot-cold
 *   Settings   → theme / export / import / backup / restore / clear / about
 */
enum class AICpDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Filled.Home),
    ENTER("enter", "Enter", Icons.Filled.AddCircle),
    HISTORY("history", "History", Icons.Filled.History),
    ANALYTICS("analytics", "Analytics", Icons.Filled.Analytics),
    SETTINGS("settings", "Settings", Icons.Filled.Settings);

    companion object {
        val START = DASHBOARD
        val ALL = entries
    }
}
