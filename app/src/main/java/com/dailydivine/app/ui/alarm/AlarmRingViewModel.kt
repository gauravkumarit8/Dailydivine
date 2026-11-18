package com.dailydivine.app.ui.alarm

import com.dailydivine.app.util.VersePreview
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

    /** F004-R15: the first sentence of the verse (see [VersePreview], which also handles the Hindi danda). */
    private fun firstSentence(rawText: String): String = VersePreview.firstSentence(rawText)
}
