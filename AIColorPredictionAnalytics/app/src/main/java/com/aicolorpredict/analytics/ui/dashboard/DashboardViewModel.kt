package com.aicolorpredict.analytics.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.AccuracyMetrics
import com.aicolorpredict.analytics.domain.model.Prediction
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.domain.usecase.PredictUseCase
import com.aicolorpredict.analytics.domain.usecase.UpdateModelPerformanceUseCase
import com.aicolorpredict.analytics.metrics.MetricsCalculator
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
    val latestPrediction: Prediction? = null,
    val recentRounds: List<Round> = emptyList(),
    val systemMetrics: AccuracyMetrics = AccuracyMetrics.EMPTY,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val predictUseCase: PredictUseCase,
    private val updatePerformanceUseCase: UpdateModelPerformanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState(isLoading = true))
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            roundRepo.observeLastN(10).collectLatest { rounds ->
                val total = roundRepo.count()
                _state.value = _state.value.copy(
                    recentRounds = rounds,
                    totalRounds = total,
                    isLoading = false
                )
                refreshMetrics()
            }
        }
    }

    fun refreshPrediction() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val prediction = predictUseCase()
                _state.value = _state.value.copy(latestPrediction = prediction, isLoading = false)
                updatePerformanceUseCase()
                refreshMetrics()
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }

    private suspend fun refreshMetrics() {
        val perModel = predictionRepo.allModelPerformance()
        val sys = MetricsCalculator.systemMetrics(perModel)
        _state.value = _state.value.copy(systemMetrics = sys)
    }
}
