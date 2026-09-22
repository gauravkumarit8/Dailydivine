package com.dailydivine.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.audio.TTSManager
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.BookmarkRepository
import com.dailydivine.app.data.repository.StreakRepository
import com.dailydivine.app.data.repository.VerseRepository
import com.dailydivine.app.domain.model.DailyVerse
import com.dailydivine.app.domain.model.StreakInfo
import com.dailydivine.app.util.ReligionMeta
import com.dailydivine.app.util.Religions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val religion: ReligionMeta? = null,
    val dailyVerse: DailyVerse? = null,
    val streak: StreakInfo? = null,
    val isLoading: Boolean = true,
    val isSpeaking: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val verseRepository: VerseRepository,
    private val streakRepository: StreakRepository,
    private val bookmarkRepository: BookmarkRepository,
    @ApplicationContext appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // F005: owned by the ViewModel (not the Composable) so playback survives
    // recomposition and is cleanly shut down exactly once, in onCleared().
    private val ttsManager = TTSManager(appContext)
    private var ttsReady = false

    init {
        ttsManager.initialize { ttsReady = true }
    }

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

    /** F005-R07: play/stop the daily verse via on-device TTS. */
    fun togglePlayVerse() {
        val verse = _uiState.value.dailyVerse?.verse ?: return
        if (_uiState.value.isSpeaking) {
            ttsManager.stop()
            _uiState.value = _uiState.value.copy(isSpeaking = false)
        } else if (ttsReady) {
            ttsManager.speak(verse.translatedText, Locale.getDefault())
            _uiState.value = _uiState.value.copy(isSpeaking = true)
            // TTSManager doesn't expose a completion callback yet (follow-up);
            // poll briefly is overkill for now, so isSpeaking just reflects
            // "we asked it to speak" rather than tracking true engine state.
        }
    }

    /** F007: bookmark toggle on the daily verse. */
    fun toggleBookmark() {
        val daily = _uiState.value.dailyVerse ?: return
        viewModelScope.launch {
            bookmarkRepository.toggle(daily.verse.id, daily.isBookmarked)
            _uiState.value = _uiState.value.copy(
                dailyVerse = daily.copy(isBookmarked = !daily.isBookmarked)
            )
        }
    }

    override fun onCleared() {
        ttsManager.shutdown()
        super.onCleared()
    }
}
