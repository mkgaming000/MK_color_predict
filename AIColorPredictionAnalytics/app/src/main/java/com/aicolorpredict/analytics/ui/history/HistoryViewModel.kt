package com.aicolorpredict.analytics.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.PredictionRepository
import com.aicolorpredict.analytics.data.repository.RoundRepository
import com.aicolorpredict.analytics.domain.model.ModelOutput
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.util.AppDispatchers
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
    val selectedRoundId: Long? = null,
    val selectedPredictions: List<ModelOutput> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val roundRepo: RoundRepository,
    private val predictionRepo: PredictionRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState(isLoading = true))
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            roundRepo.observeLastN(100).collectLatest { rounds ->
                _state.value = _state.value.copy(
                    rounds = rounds,
                    totalRounds = rounds.size,
                    isLoading = false
                )
            }
        }
    }

    fun select(roundId: Long) {
        viewModelScope.launch {
            val preds = predictionRepo.getByRound(roundId)
            _state.value = _state.value.copy(selectedRoundId = roundId, selectedPredictions = preds)
        }
    }
}
