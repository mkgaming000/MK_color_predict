package com.aicolorpredict.analytics.ui.transition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.domain.model.TransitionStats
import com.aicolorpredict.analytics.domain.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransitionUiState(
    val isLoading: Boolean = false,
    val fromNumber: Int = 0,
    val stats: TransitionStats? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TransitionViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransitionUiState(isLoading = true, fromNumber = 0))
    val state: StateFlow<TransitionUiState> = _state.asStateFlow()

    init { load(0) }

    fun load(from: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, fromNumber = from, errorMessage = null)
            try {
                val stats = searchUseCase.transitionStats(from)
                _state.value = _state.value.copy(isLoading = false, stats = stats)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }
}
