package com.aicolorpredict.analytics.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicolorpredict.analytics.data.repository.ColorRoundRepository
import com.aicolorpredict.analytics.domain.model.AppColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val redCount: Int = 0,
    val greenCount: Int = 0,
    val totalRounds: Int = 0,
    val redAfterRed: Int = 0,
    val redAfterGreen: Int = 0,
    val greenAfterRed: Int = 0,
    val greenAfterGreen: Int = 0,
    val currentStreak: Int = 0,
    val currentStreakColor: AppColor = AppColor.RED,
    val longestRedStreak: Int = 0,
    val longestGreenStreak: Int = 0,
    val recentRedPct: Double = 0.5
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val roundRepo: ColorRoundRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsUiState(isLoading = true))
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val rounds = roundRepo.lastN(2000)
            if (rounds.isEmpty()) {
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }
            var red = 0; var green = 0
            var rAfterR = 0; var rAfterG = 0; var gAfterR = 0; var gAfterG = 0
            var longestR = 0; var longestG = 0; var curStreak = 1
            for (i in rounds.indices) {
                if (rounds[i].color == AppColor.RED) red++ else green++
                if (i > 0) {
                    val prev = rounds[i - 1].color
                    val cur = rounds[i].color
                    if (prev == AppColor.RED && cur == AppColor.RED) rAfterR++
                    if (prev == AppColor.GREEN && cur == AppColor.RED) rAfterG++
                    if (prev == AppColor.RED && cur == AppColor.GREEN) gAfterR++
                    if (prev == AppColor.GREEN && cur == AppColor.GREEN) gAfterG++
                    if (cur == prev) curStreak++ else curStreak = 1
                }
                if (rounds[i].color == AppColor.RED) longestR = maxOf(longestR, curStreak) else longestG = maxOf(longestG, curStreak)
            }
            val recent = rounds.takeLast(minOf(50, rounds.size))
            val recentRed = recent.count { it.color == AppColor.RED }.toDouble() / recent.size
            _state.value = AnalyticsUiState(
                isLoading = false,
                redCount = red, greenCount = green, totalRounds = rounds.size,
                redAfterRed = rAfterR, redAfterGreen = rAfterG, greenAfterRed = gAfterR, greenAfterGreen = gAfterG,
                currentStreak = curStreak, currentStreakColor = rounds.last().color,
                longestRedStreak = longestR, longestGreenStreak = longestG,
                recentRedPct = recentRed
            )
        }
    }
}
