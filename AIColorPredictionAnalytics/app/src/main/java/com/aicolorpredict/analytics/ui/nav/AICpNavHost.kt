package com.aicolorpredict.analytics.ui.nav

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
import com.aicolorpredict.analytics.ui.enter.EnterScreen
import com.aicolorpredict.analytics.ui.history.HistoryScreen
import com.aicolorpredict.analytics.ui.settings.SettingsScreen

@Composable
fun AICpNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: AICpDestination.START.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                AICpDestination.ALL.forEach { d ->
                    NavigationBarItem(
                        selected = currentRoute == d.route,
                        onClick = {
                            navController.navigate(d.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(d.icon, contentDescription = d.label, modifier = Modifier.size(24.dp)) },
                        label = { Text(d.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
            composable(AICpDestination.ENTER.route) { EnterScreen() }
            composable(AICpDestination.HISTORY.route) { HistoryScreen() }
            composable(AICpDestination.ANALYTICS.route) { AnalyticsScreen() }
            composable(AICpDestination.SETTINGS.route) { SettingsScreen() }
        }
    }
}
