package com.aicolorpredict.analytics.ui.enter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.domain.model.AppColor
import com.aicolorpredict.analytics.domain.usecase.AddColorRoundUseCase
import com.aicolorpredict.analytics.domain.usecase.PredictColorUseCase
import com.aicolorpredict.analytics.domain.usecase.UpdateColorPerformanceUseCase
import com.aicolorpredict.analytics.ai.color.ColorModelRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnterUiState(
    val isSaving: Boolean = false,
    val savedColor: AppColor? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class EnterViewModel @Inject constructor(
    private val addColorRoundUseCase: AddColorRoundUseCase,
    private val updatePerformanceUseCase: UpdateColorPerformanceUseCase,
    private val predictUseCase: PredictColorUseCase,
    private val registry: ColorModelRegistry
) : ViewModel() {

    private val _state = MutableStateFlow(EnterUiState())
    val state: StateFlow<EnterUiState> = _state.asStateFlow()

    fun saveColor(color: AppColor) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            try {
                addColorRoundUseCase(color)
                updatePerformanceUseCase(registry.names)
                predictUseCase()
                _state.value = _state.value.copy(isSaving = false, savedColor = color)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isSaving = false, errorMessage = t.message)
            }
        }
    }

    fun consumeSaved() { _state.value = _state.value.copy(savedColor = null) }
    fun consumeError() { _state.value = _state.value.copy(errorMessage = null) }
}
