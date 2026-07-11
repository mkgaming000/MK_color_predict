package com.aicolorpredict.analytics.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.AccuracyMetrics
import com.aicolorpredict.analytics.domain.model.Prediction
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.domain.usecase.AddRoundUseCase
import com.aicolorpredict.analytics.domain.usecase.PredictUseCase
import com.aicolorpredict.analytics.domain.usecase.UpdateModelPerformanceUseCase
import android.util.Log
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

/**
 * Dashboard ViewModel.
 *
 * Owns the "current prediction" surface for the home screen. Also exposes a
 * [quickAddRound] method so the home screen's quick-add button can save a new
 * round and immediately retrain — this avoids a round-trip to the Enter screen
 * for the common case of "the result just came in, log it now".
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val predictUseCase: PredictUseCase,
    private val addRoundUseCase: AddRoundUseCase,
    private val updatePerformanceUseCase: UpdateModelPerformanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState(isLoading = false))
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe recent rounds so the dashboard updates live.
            roundRepo.observeLastN(10).collectLatest { rounds ->
                val total = roundRepo.count()
                _state.value = _state.value.copy(
                    recentRounds = rounds,
                    totalRounds = total,
                    isLoading = false
                )
                refreshMetrics()
                // Auto-generate a prediction if we don't have one yet.
                if (_state.value.latestPrediction == null && rounds.isNotEmpty()) {
                    refreshPrediction()
                }
            }
        }
    }

    /**
     * Quick-add a round from the dashboard. Resolves the previous prediction,
     * updates model performance, then generates a fresh prediction for the
     * *next* round.
     */
    fun quickAddRound(number: Int) {
        Log.d("DashboardVM", "Quick add round: $number")
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                addRoundUseCase(number)
                updatePerformanceUseCase()
                refreshPrediction()
                refreshMetrics()
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }

    fun refreshPrediction() {
        Log.d("DashboardVM", "Refresh prediction button clicked")
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
