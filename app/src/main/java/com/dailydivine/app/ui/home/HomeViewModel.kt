package com.dailydivine.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.StreakRepository
import com.dailydivine.app.data.repository.VerseRepository
import com.dailydivine.app.domain.model.DailyVerse
import com.dailydivine.app.domain.model.StreakInfo
import com.dailydivine.app.util.ReligionMeta
import com.dailydivine.app.util.Religions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val religion: ReligionMeta? = null,
    val dailyVerse: DailyVerse? = null,
    val streak: StreakInfo? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val verseRepository: VerseRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Reads the real religion + install date persisted during onboarding
     * (closes the Sprint 2 gap where this screen read a hardcoded
     * religionId = 1 placeholder regardless of what the user actually
     * selected). Falls back to religion #1 / today only if prefs are
     * somehow missing, which shouldn't happen once onboarding has run, but
     * keeps this screen from crashing rather than assuming happy path.
     */
    fun load() {
        viewModelScope.launch {
            val prefs = userPreferences.state.first()
            val religionId = prefs.religionId ?: Religions.ALL.first().id
            val installDate = prefs.installEpochDay
                ?.let { LocalDate.ofEpochDay(it) }
                ?: LocalDate.now()

            val verse = verseRepository.getDailyVerse(religionId, installDate, LocalDate.now())
            verse?.let { streakRepository.recordOpenedToday(it.verse.id) }
            val streak = streakRepository.calculateStreak()

            _uiState.value = HomeUiState(
                religion = Religions.byId(religionId),
                dailyVerse = verse,
                streak = streak,
                isLoading = false
            )
        }
    }
}
