package com.aicolorpredict.analytics.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.ColorRoundRepository
import com.aicolorpredict.analytics.domain.model.ColorPrediction
import com.aicolorpredict.analytics.domain.model.ColorRound
import com.aicolorpredict.analytics.domain.usecase.PredictColorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalRounds: Int = 0,
    val latestPrediction: ColorPrediction? = null,
    val recentRounds: List<ColorRound> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val roundRepo: ColorRoundRepository,
    private val predictUseCase: PredictColorUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState(isLoading = false))
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            roundRepo.observeLastN(10).collectLatest { rounds ->
                _state.value = _state.value.copy(recentRounds = rounds, totalRounds = rounds.size)
                if (_state.value.latestPrediction == null && rounds.isNotEmpty()) {
                    refreshPrediction()
                }
            }
        }
    }

    fun refreshPrediction() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val pred = predictUseCase()
                _state.value = _state.value.copy(latestPrediction = pred, isLoading = false)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message)
            }
        }
    }
}
