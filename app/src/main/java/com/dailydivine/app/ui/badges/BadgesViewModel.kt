package com.dailydivine.app.ui.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.repository.StreakRepository
import com.dailydivine.app.domain.model.StreakInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BadgesUiState(val streak: StreakInfo? = null)

@HiltViewModel
class BadgesViewModel @Inject constructor(
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BadgesUiState())
    val state: StateFlow<BadgesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { _state.value = BadgesUiState(streakRepository.calculateStreak()) }
    }
}
