package com.aicolorpredict.analytics.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val appVersion: String = "1.0.0",
    val darkTheme: Boolean = true,
    val useDynamicColor: Boolean = false,
    val showDisclaimer: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setDarkTheme(v: Boolean) { _state.value = _state.value.copy(darkTheme = v) }
    fun setDynamicColor(v: Boolean) { _state.value = _state.value.copy(useDynamicColor = v) }
    fun setShowDisclaimer(v: Boolean) { _state.value = _state.value.copy(showDisclaimer = v) }
}
