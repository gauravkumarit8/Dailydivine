package com.dailydivine.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.repository.StreakRepository
import com.dailydivine.app.data.repository.VerseRepository
import com.dailydivine.app.domain.model.DailyVerse
import com.dailydivine.app.domain.model.StreakInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val dailyVerse: DailyVerse? = null,
    val streak: StreakInfo? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val verseRepository: VerseRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** @param religionId user's selected religion (from UserProfile / DataStore)
     *  @param installDate anchors the deterministic day-number algorithm (API Contract 1) */
    fun load(religionId: Int, installDate: LocalDate) {
        viewModelScope.launch {
            val verse = verseRepository.getDailyVerse(religionId, installDate, LocalDate.now())
            verse?.let { streakRepository.recordOpenedToday(it.verse.id) }
            val streak = streakRepository.calculateStreak()
            _uiState.value = HomeUiState(dailyVerse = verse, streak = streak, isLoading = false)
        }
    }
}
