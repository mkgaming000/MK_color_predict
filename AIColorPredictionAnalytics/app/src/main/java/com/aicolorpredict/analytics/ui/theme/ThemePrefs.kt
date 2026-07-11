package com.aicolorpredict.analytics.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide theme preference holder.
 *
 * Kept as a process-lifetime singleton (not persisted to DataStore in this
 * iteration — the values reset on app restart). Persistence can be added
 * later by swapping the backing store to DataStore without changing call sites.
 */
object ThemePrefs {
    private val _darkMode = MutableStateFlow(true)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(true)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    fun setDarkMode(v: Boolean) { _darkMode.value = v }
    fun setDynamicColor(v: Boolean) { _dynamicColor.value = v }
}

/**
 * Convenience wrapper around [AICpTheme] that reads the current user prefs.
 */
@Composable
fun AICpAppTheme(content: @Composable () -> Unit) {
    val dark by ThemePrefs.darkMode.collectAsState()
    val dynamic by ThemePrefs.dynamicColor.collectAsState()
    AICpTheme(darkTheme = dark, dynamicColor = dynamic, content = content)
}
