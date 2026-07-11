package com.aicolorpredict.analytics.ui.enter

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.usecase.AddRoundUseCase
import com.aicolorpredict.analytics.domain.usecase.PredictUseCase
import com.aicolorpredict.analytics.domain.usecase.UpdateModelPerformanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnterUiState(
    val selectedNumber: Int? = null,
    val epochMs: Long = System.currentTimeMillis(),
    val roundNumber: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Enter Result screen ViewModel.
 *
 * Handles the save-and-retrain cycle: when the user taps Save, we
 *   1. add the round (which resolves the previous prediction),
 *   2. update model performance metrics,
 *   3. generate a fresh prediction for the new "next" round.
 *
 * The dashboard's Flow observer picks up the new round automatically — no
 * explicit cross-screen refresh needed.
 */
@HiltViewModel
class EnterViewModel @Inject constructor(
    application: Application,
    private val addRoundUseCase: AddRoundUseCase,
    private val updatePerformanceUseCase: UpdateModelPerformanceUseCase,
    private val predictUseCase: PredictUseCase
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(EnterUiState())
    val state: StateFlow<EnterUiState> = _state.asStateFlow()

    fun selectNumber(n: Int) {
        _state.value = _state.value.copy(selectedNumber = n, saved = false, errorMessage = null)
    }

    fun setEpochMs(ms: Long) {
        _state.value = _state.value.copy(epochMs = ms)
    }

    fun setRoundNumber(v: String) {
        _state.value = _state.value.copy(roundNumber = v.filter { it.isDigit() }.take(10))
    }

    fun save() {
        val number = _state.value.selectedNumber ?: run {
            _state.value = _state.value.copy(errorMessage = "Select a number first")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null, saved = false)
            try {
                addRoundUseCase(number, _state.value.epochMs)
                updatePerformanceUseCase()
                // Generate a fresh prediction for the new "next" round so the
                // dashboard is immediately up to date when the user navigates.
                predictUseCase()
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isSaving = false, errorMessage = t.message ?: "Save failed")
            }
        }
    }

    fun resetSaved() {
        _state.value = _state.value.copy(saved = false, selectedNumber = null)
    }

    fun consumeError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
