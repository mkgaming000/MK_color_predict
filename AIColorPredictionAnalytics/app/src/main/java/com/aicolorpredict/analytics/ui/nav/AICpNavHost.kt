package com.aicolorpredict.analytics.ui.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aicolorpredict.analytics.ui.analytics.AnalyticsScreen
import com.aicolorpredict.analytics.ui.dashboard.DashboardScreen
import com.aicolorpredict.analytics.ui.data.DataScreen
import com.aicolorpredict.analytics.ui.history.HistoryScreen
import com.aicolorpredict.analytics.ui.models.ModelsScreen
import com.aicolorpredict.analytics.ui.prediction.PredictionScreen
import com.aicolorpredict.analytics.ui.search.SearchScreen
import com.aicolorpredict.analytics.ui.settings.SettingsScreen
import com.aicolorpredict.analytics.ui.transition.TransitionScreen

@Composable
fun AICpNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: AICpDestination.START.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                tonalElevation = 0.dp
            ) {
                val visible = listOf(
                    AICpDestination.DASHBOARD,
                    AICpDestination.PREDICTION,
                    AICpDestination.HISTORY,
                    AICpDestination.ANALYTICS,
                    AICpDestination.TRANSITION,
                    AICpDestination.MODELS,
                    AICpDestination.SEARCH,
                    AICpDestination.DATA,
                    AICpDestination.SETTINGS
                )
                visible.forEach { d ->
                    NavigationBarItem(
                        selected = currentRoute == d.route,
                        onClick = {
                            navController.navigate(d.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(d.icon, contentDescription = d.label, modifier = Modifier.size(22.dp)) },
                        label = { Text(d.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AICpDestination.START.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AICpDestination.DASHBOARD.route) { DashboardScreen() }
            composable(AICpDestination.PREDICTION.route) { PredictionScreen() }
            composable(AICpDestination.HISTORY.route) { HistoryScreen() }
            composable(AICpDestination.ANALYTICS.route) { AnalyticsScreen() }
            composable(AICpDestination.TRANSITION.route) { TransitionScreen() }
            composable(AICpDestination.MODELS.route) { ModelsScreen() }
            composable(AICpDestination.SEARCH.route) { SearchScreen() }
            composable(AICpDestination.DATA.route) { DataScreen() }
            composable(AICpDestination.SETTINGS.route) { SettingsScreen() }
        }
    }
}
