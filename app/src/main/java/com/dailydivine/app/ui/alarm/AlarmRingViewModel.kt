package com.dailydivine.app.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.VerseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AlarmRingViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val verseRepository: VerseRepository
) : ViewModel() {

    /** Null while loading or if there's genuinely no verse content for the
     *  user's religion yet -- AlarmRingScreen shows the generic greeting
     *  alone in either case, same graceful-empty-state pattern as Home. */
    private val _versePreview = MutableStateFlow<String?>(null)
    val versePreview: StateFlow<String?> = _versePreview.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = userPreferences.state.first()
            val religionId = prefs.religionId ?: return@launch
            val installDate = prefs.installEpochDay
                ?.let { LocalDate.ofEpochDay(it) }
                ?: LocalDate.now()

            val verse = verseRepository.getDailyVerse(religionId, installDate, LocalDate.now())
            _versePreview.value = verse?.verse?.translatedText?.let { firstSentence(it) }
        }
    }

    /** F004-R15 wants just "the first line" -- approximated here as the
     *  first sentence (up to the first '.', '!', or '?'), capped at 120
     *  chars so an unusually long first sentence doesn't overflow the
     *  ring screen's limited space. */
    private fun firstSentence(text: String): String {
        val endIdx = text.indexOfFirst { it == '.' || it == '!' || it == '?' }
        val candidate = if (endIdx in 0 until 200) text.substring(0, endIdx + 1) else text
        return if (candidate.length > 120) candidate.take(117) + "..." else candidate
    }
}
