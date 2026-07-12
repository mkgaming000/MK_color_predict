package com.aicolorpredict.analytics.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.ModelOutput
import android.util.Log
import com.aicolorpredict.analytics.domain.model.Round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = false,
    val rounds: List<Round> = emptyList(),
    val totalRounds: Int = 0,
    val pageSize: Int = 100,
    val expandedRoundId: Long? = null,
    val predictionsForExpanded: List<ModelOutput> = emptyList(),
    val errorMessage: String? = null
)

/**
 * History ViewModel — paginated.
 *
 * Loads 100 rounds at a time (configurable via [pageSize]). The total count
 * comes from `observeCount()` so the UI can show "showing 100 of 10,000".
 *
 * The [rounds] list is always the most recent `pageSize` rounds — older
 * history is accessed via the page() method if needed in future.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState(isLoading = true))
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe the most recent 100 rounds for the timeline.
            roundRepo.observeLastN(_state.value.pageSize).collectLatest { rounds ->
                _state.value = _state.value.copy(
                    rounds = rounds,
                    isLoading = false
                )
            }
        }
        // Observe total count separately so the UI knows the full history size.
        viewModelScope.launch {
            roundRepo.observeCount().collectLatest { count ->
                _state.value = _state.value.copy(totalRounds = count)
            }
        }
    }

    fun toggleExpand(roundId: Long) {
        Log.d("HistoryVM", "Toggle expand: roundId=$roundId")
        if (_state.value.expandedRoundId == roundId) {
            _state.value = _state.value.copy(expandedRoundId = null, predictionsForExpanded = emptyList())
        } else {
            _state.value = _state.value.copy(expandedRoundId = roundId)
            viewModelScope.launch {
                try {
                    val preds = predictionRepo.getByRound(roundId)
                    _state.value = _state.value.copy(predictionsForExpanded = preds)
                } catch (t: Throwable) {
                    _state.value = _state.value.copy(errorMessage = t.message)
                }
            }
        }
    }
}
