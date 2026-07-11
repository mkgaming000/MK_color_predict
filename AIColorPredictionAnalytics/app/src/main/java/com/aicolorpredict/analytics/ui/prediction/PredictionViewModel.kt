package com.aicolorpredict.analytics.ui.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.domain.model.Prediction
import com.aicolorpredict.analytics.domain.usecase.PredictUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PredictionUiState(
    val isLoading: Boolean = false,
    val prediction: Prediction? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val predictUseCase: PredictUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PredictionUiState())
    val state: StateFlow<PredictionUiState> = _state.asStateFlow()

    fun predict() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val p = predictUseCase()
                _state.value = _state.value.copy(prediction = p, isLoading = false)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }
}
