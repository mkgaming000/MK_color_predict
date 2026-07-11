package com.aicolorpredict.analytics.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.domain.model.Round
import com.aicolorpredict.analytics.domain.usecase.SearchUseCase
import com.aicolorpredict.analytics.util.AppDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val mode: SearchMode = SearchMode.NUMBER,
    val results: List<Round> = emptyList(),
    val errorMessage: String? = null
)

enum class SearchMode { ROUND, NUMBER, PATTERN }

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    fun setMode(mode: SearchMode) {
        _state.value = _state.value.copy(mode = mode, results = emptyList(), errorMessage = null)
    }

    fun updateQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun search() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val q = state.value.query.trim()
                val results: List<Round> = when (state.value.mode) {
                    SearchMode.ROUND -> {
                        val id = q.toLongOrNull() ?: return@launch finishError("Round ID must be a number")
                        listOfNotNull(searchUseCase.byRoundId(id))
                    }
                    SearchMode.NUMBER -> {
                        val n = q.toIntOrNull() ?: return@launch finishError("Number must be 0..9")
                        if (n !in 0..9) return@launch finishError("Number must be 0..9")
                        searchUseCase.byNumber(n)
                    }
                    SearchMode.PATTERN -> {
                        val pat = q.split(",", " ", "-").mapNotNull { it.trim().toIntOrNull() }
                            .filter { it in 0..9 }
                        if (pat.isEmpty()) return@launch finishError("Pattern must be comma-separated numbers 0..9")
                        searchUseCase.byPattern(pat)
                    }
                }
                _state.value = _state.value.copy(isLoading = false, results = results)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }

    private fun finishError(msg: String) {
        _state.value = _state.value.copy(isLoading = false, errorMessage = msg)
    }
}
