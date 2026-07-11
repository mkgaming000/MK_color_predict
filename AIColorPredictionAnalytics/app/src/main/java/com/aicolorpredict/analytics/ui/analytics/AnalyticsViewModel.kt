package com.aicolorpredict.analytics.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.AccuracyMetrics
import com.aicolorpredict.analytics.domain.model.ModelPerformance
import com.aicolorpredict.analytics.metrics.MetricsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val systemMetrics: AccuracyMetrics = AccuracyMetrics.EMPTY,
    val perModel: List<ModelPerformance> = emptyList(),
    val numberHistogram: Map<Int, Int> = emptyMap(),
    val errorMessage: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsUiState(isLoading = true))
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val perModel = predictionRepo.allModelPerformance()
                val sys = MetricsCalculator.systemMetrics(perModel)
                val hist = roundRepo.numberHistogram()
                _state.value = _state.value.copy(
                    isLoading = false,
                    systemMetrics = sys,
                    perModel = perModel.sortedByDescending { it.top1Accuracy },
                    numberHistogram = hist
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }
}
