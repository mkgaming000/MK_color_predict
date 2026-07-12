package com.aicolorpredict.analytics.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.ColorRoundRepository
import com.aicolorpredict.analytics.domain.model.ColorRound
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = false,
    val rounds: List<ColorRound> = emptyList(),
    val totalRounds: Int = 0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val roundRepo: ColorRoundRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState(isLoading = true))
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            roundRepo.observeLastN(100).collectLatest { rounds ->
                _state.value = _state.value.copy(rounds = rounds, isLoading = false)
            }
        }
        viewModelScope.launch {
            roundRepo.observeCount().collectLatest { count ->
                _state.value = _state.value.copy(totalRounds = count)
            }
        }
    }
}
