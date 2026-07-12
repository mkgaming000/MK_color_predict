package com.aicolorpredict.analytics.ui.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.ColorRoundRepository
import com.aicolorpredict.analytics.data.repository.ColorPredictionRepository
import com.aicolorpredict.analytics.ui.theme.ThemePrefs
import com.aicolorpredict.analytics.util.AppDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import javax.inject.Inject

data class SettingsUiState(
    val darkMode: Boolean = true,
    val dynamicColor: Boolean = true,
    val totalRounds: Int = 0,
    val lastAction: String? = null,
    val errorMessage: String? = null,
    val showClearDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val roundRepo: ColorRoundRepository,
    private val predictionRepo: ColorPredictionRepository,
    private val dispatchers: AppDispatchers
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SettingsUiState(
        darkMode = ThemePrefs.darkMode.value,
        dynamicColor = ThemePrefs.dynamicColor.value
    ))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            roundRepo.observeCount().collect { _state.value = _state.value.copy(totalRounds = it) }
        }
    }

    fun setDarkMode(v: Boolean) { ThemePrefs.setDarkMode(v); _state.value = _state.value.copy(darkMode = v) }
    fun setDynamicColor(v: Boolean) { ThemePrefs.setDynamicColor(v); _state.value = _state.value.copy(dynamicColor = v) }
    fun showClearDialog(v: Boolean) { _state.value = _state.value.copy(showClearDialog = v) }

    fun clearAll() {
        viewModelScope.launch(dispatchers.io) {
            try {
                roundRepo.clearAll()
                predictionRepo.clearAll()
                _state.value = _state.value.copy(lastAction = "All data cleared", showClearDialog = false)
            } catch (t: Throwable) { _state.value = _state.value.copy(errorMessage = t.message, showClearDialog = false) }
        }
    }
}
