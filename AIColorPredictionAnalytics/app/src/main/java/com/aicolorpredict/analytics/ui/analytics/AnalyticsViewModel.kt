package com.aicolorpredict.analytics.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.AccuracyMetrics
import com.aicolorpredict.analytics.feature.TransitionAnalytics
import com.aicolorpredict.analytics.domain.model.TransitionStats
import android.util.Log
import com.aicolorpredict.analytics.metrics.MetricsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val numberFrequency: Map<Int, Int> = emptyMap(),
    val hotNumbers: List<Int> = emptyList(),
    val coldNumbers: List<Int> = emptyList(),
    val transitionStats: TransitionStats? = null,
    val transitionFrom: Int = 0,
    val systemMetrics: AccuracyMetrics = AccuracyMetrics.EMPTY,
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
                val hist = roundRepo.numberHistogram()
                val total = hist.values.sum().coerceAtLeast(1)
                val sorted = (0..9).sortedByDescending { hist[it] ?: 0 }
                val hot = sorted.take(3)
                val cold = sorted.takeLast(3).reversed()
                val perModel = predictionRepo.allModelPerformance()
                val sys = MetricsCalculator.systemMetrics(perModel)
                val history = roundRepo.lastN(2000).map { it.number }
                val trans = TransitionAnalytics.build(_state.value.transitionFrom, history)
                _state.value = _state.value.copy(
                    isLoading = false,
                    numberFrequency = hist,
                    hotNumbers = hot,
                    coldNumbers = cold,
                    transitionStats = trans,
                    systemMetrics = sys
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }

    fun setTransitionFrom(n: Int) {
        Log.d("AnalyticsVM", "Transition from: $n")
        viewModelScope.launch {
            _state.value = _state.value.copy(transitionFrom = n)
            val history = roundRepo.lastN(2000).map { it.number }
            val trans = TransitionAnalytics.build(n, history)
            _state.value = _state.value.copy(transitionStats = trans)
        }
    }
}
