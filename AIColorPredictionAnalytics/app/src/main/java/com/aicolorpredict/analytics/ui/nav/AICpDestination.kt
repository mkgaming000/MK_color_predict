package com.aicolorpredict.analytics.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.vector.ImageVector

enum class AICpDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Filled.Insights),
    PREDICTION("prediction", "Predict", Icons.Filled.Speed),
    HISTORY("history", "History", Icons.Filled.History),
    ANALYTICS("analytics", "Analytics", Icons.Filled.Analytics),
    TRANSITION("transition", "Transitions", Icons.Filled.Hub),
    MODELS("models", "Models", Icons.Filled.AccountTree),
    SEARCH("search", "Search", Icons.Filled.Search),
    DATA("data", "Data", Icons.Filled.Backup),
    SETTINGS("settings", "Settings", Icons.Filled.Settings);

    companion object {
        val START = DASHBOARD
    }
}
