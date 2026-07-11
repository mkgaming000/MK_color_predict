package com.aicolorpredict.analytics.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.ai.base.ModelRegistry
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.domain.model.ModelPerformance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelsUiState(
    val isLoading: Boolean = false,
    val registeredModels: List<String> = emptyList(),
    val performance: List<ModelPerformance> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ModelsViewModel @Inject constructor(
    private val registry: ModelRegistry,
    private val predictionRepo: PredictionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ModelsUiState(isLoading = true))
    val state: StateFlow<ModelsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val perf = predictionRepo.allModelPerformance()
                _state.value = _state.value.copy(
                    isLoading = false,
                    registeredModels = registry.names,
                    performance = perf
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }
}
