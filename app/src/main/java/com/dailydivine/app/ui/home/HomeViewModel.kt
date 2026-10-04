package com.dailydivine.app.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.audio.SpeakResult
import com.dailydivine.app.audio.TTSManager
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.BookmarkRepository
import com.dailydivine.app.data.repository.JournalRepository
import com.dailydivine.app.data.repository.StreakRepository
import com.dailydivine.app.data.repository.VerseRepository
import com.dailydivine.app.domain.model.DailyVerse
import com.dailydivine.app.domain.model.MilestoneBadge
import com.dailydivine.app.notifications.NotificationHelper
import com.dailydivine.app.domain.model.StreakInfo
import com.dailydivine.app.util.ReligionMeta
import com.dailydivine.app.util.Religions
import com.dailydivine.app.util.ShareImageGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val religion: ReligionMeta? = null,
    val dailyVerse: DailyVerse? = null,
    val streak: StreakInfo? = null,
    val isLoading: Boolean = true,
    val isSpeaking: Boolean = false,
    /** Set once when a streak milestone is first reached (F009-R10); cleared on dismiss. */
    val milestoneReached: MilestoneBadge? = null,
    /** F011: today's reflection text and optional mood emoji. */
    val reflection: String = "",
    val mood: String? = null,
    /** One-shot message for the UI (e.g. no TTS voice installed); cleared after it is shown. */
    val ttsMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val verseRepository: VerseRepository,
    private val streakRepository: StreakRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val journalRepository: JournalRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // F005: owned by the ViewModel (not the Composable) so playback survives
    // recomposition and is cleanly shut down exactly once, in onCleared().
    private val ttsManager = TTSManager(appContext)
    @Volatile private var ttsReady = false
    // Declared above init on purpose (see the ORDER MATTERS note below).
    private var autoReadDone = false

    private val shareImageGenerator = ShareImageGenerator(appContext)

    // ORDER MATTERS: these must be declared ABOVE the init block. Kotlin
    // initialises properties top to bottom, and init starts collecting prefs.
    // DataStore can emit an already-cached value without suspending, so the
    // collector can reach loadMutex before a declaration placed below init has
    // run, and that is a NullPointerException inside this constructor, i.e. a
    // crash the moment Home opens.
    // The init collector and refresh() (app start) can fire together; without
    // serialising, both could record "opened today" and both see a milestone
    // as new. One load at a time.
    private val loadMutex = Mutex()

    // F011 journal autosave state (see onReflectionChanged).
    private var saveJob: Job? = null
    private var pendingDate: LocalDate? = null

    init {
        ttsManager.initialize { ttsReady = true }

        // Reactively observes religion/install-date instead of a one-shot
        // load() -- previously, switching religion in Settings wouldn't
        // update Home until the app was force-closed and reopened, since
        // load() only ever ran once when the screen first composed and this
        // ViewModel instance is retained across bottom-nav tab switches
        // (hiltViewModel() scopes it to the Home destination's back stack
        // entry, which survives navigating away and back). Now any change
        // to the persisted religion/install date automatically triggers a
        // fresh verse + streak load, visible the moment the user returns to
        // the Home tab.
        viewModelScope.launch {
            userPreferences.state
                .map { it.religionId to it.installEpochDay }
                .distinctUntilChanged()
                .collect { (religionIdRaw, installEpochDay) ->
                    loadVerseAndStreak(religionIdRaw, installEpochDay)
                }
        }
    }

    private suspend fun loadVerseAndStreak(religionIdRaw: Int?, installEpochDay: Long?) =
        loadMutex.withLock { loadVerseAndStreakLocked(religionIdRaw, installEpochDay) }

    private suspend fun loadVerseAndStreakLocked(religionIdRaw: Int?, installEpochDay: Long?) {
        val religionId = religionIdRaw ?: Religions.ALL.first().id
        val installDate = installEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()

        val verse = verseRepository.getDailyVerse(religionId, installDate, LocalDate.now())
        verse?.let { streakRepository.recordOpenedToday(it.verse.id) }
        // F011 AC2: reopening the app the same day shows the saved reflection.
        // If the user is mid-edit (a save is pending) keep what they typed
        // instead of overwriting it with the older saved value.
        val entry = if (verse != null) journalRepository.getEntry(LocalDate.now()) else null
        val keepTyped = saveJob?.isActive == true
        val streak = streakRepository.calculateStreak()

        // F009-R10/R11: celebrate a milestone exactly once. lastMilestoneDays
        // persists the highest one already celebrated, so reloading Home (or
        // reopening the app the same day) never repeats it.
        val prefs = userPreferences.state.first()
        val reached = streak.currentMilestone
            ?.takeIf { it.days == streak.currentStreak && it.days > prefs.lastMilestoneDays }
        if (reached != null) {
            userPreferences.setLastMilestoneDays(reached.days)
            if (prefs.milestonesEnabled) NotificationHelper.postMilestone(appContext, reached.days, reached.name)
        }

        _uiState.value = _uiState.value.copy(
            religion = Religions.byId(religionId),
            dailyVerse = verse,
            streak = streak,
            isLoading = false,
            milestoneReached = reached ?: _uiState.value.milestoneReached,
            reflection = if (keepTyped) _uiState.value.reflection else (entry?.journalText ?: ""),
            mood = if (keepTyped) _uiState.value.mood else entry?.moodEmoji
        )
    }

    /** Called each time the app comes to the foreground. This ViewModel can
     *  outlive a whole day in memory, so without this the daily verse and the
     *  "opened today" streak record would go stale across midnight. Safe to
     *  repeat: recording an already-recorded day is a no-op. */
    fun refresh() {
        viewModelScope.launch {
            val prefs = userPreferences.state.first()
            loadVerseAndStreak(prefs.religionId, prefs.installEpochDay)
        }
    }

    // ---- F011 journal: debounced autosave (R03, 500 ms) ----
    fun onReflectionChanged(text: String) {
        _uiState.value = _uiState.value.copy(reflection = text)
        // Remember the day the text was written, so a save that fires just
        // after midnight still lands on the right day.
        val date = pendingDate ?: LocalDate.now().also { pendingDate = it }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            persistReflection(date)
        }
    }

    /** Mood taps are saved immediately; tapping the selected emoji clears it. */
    fun onMoodSelected(emoji: String) {
        val newMood = if (_uiState.value.mood == emoji) null else emoji
        _uiState.value = _uiState.value.copy(mood = newMood)
        val date = pendingDate ?: LocalDate.now()
        saveJob?.cancel()
        viewModelScope.launch { persistReflection(date) }
    }

    /** Called when the app stops: don't lose text typed in the last 500 ms. */
    fun flushReflection() {
        val job = saveJob ?: return
        if (!job.isActive) return
        job.cancel()
        val date = pendingDate ?: LocalDate.now()
        viewModelScope.launch { persistReflection(date) }
    }

    private suspend fun persistReflection(date: LocalDate) {
        val s = _uiState.value
        journalRepository.save(date, s.reflection, s.mood)
        pendingDate = null
    }

    fun dismissMilestone() {
        _uiState.value = _uiState.value.copy(milestoneReached = null)
    }

    /** F005-R07: play/stop the daily verse via on-device TTS. */
    fun togglePlayVerse() {
        val verse = _uiState.value.dailyVerse?.verse ?: return
        if (_uiState.value.isSpeaking) {
            ttsManager.stop()
            _uiState.value = _uiState.value.copy(isSpeaking = false)
            return
        }
        viewModelScope.launch {
            val rate = userPreferences.state.first().ttsRate // F012-R06
            _uiState.value = _uiState.value.copy(isSpeaking = true)
            // Speak in the VERSE's language, not the phone's default locale.
            val result = ttsManager.speak(
                verse.translatedText,
                Locale.forLanguageTag(verse.languageCode),
                rate = rate,
                onDone = { _uiState.value = _uiState.value.copy(isSpeaking = false) }
            )
            if (result != SpeakResult.STARTED) {
                _uiState.value = _uiState.value.copy(
                    isSpeaking = false,
                    ttsMessage = when (result) {
                        SpeakResult.ENGINE_NOT_READY -> "The voice is still starting. Try again in a moment."
                        else -> "No voice is installed for this language. Add one in your phone's text-to-speech settings."
                    }
                )
            }
        }
    }

    /** F004-R18: after "Wake Up & Read" with TTS on, read the verse once. Waits
     *  briefly for the engine (it was only just created when the app launched
     *  from the alarm) and never repeats within this ViewModel's life. */
    fun autoRead() {
        if (autoReadDone) return
        autoReadDone = true
        viewModelScope.launch {
            var waited = 0
            while (!ttsReady && waited < 5000) { delay(100); waited += 100 }
            if (!_uiState.value.isSpeaking) togglePlayVerse()
        }
    }

    fun clearTtsMessage() {
        _uiState.value = _uiState.value.copy(ttsMessage = null)
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

    /**
     * F008: generates a shareable image of the current daily verse.
     * Bitmap/Canvas work runs on Dispatchers.Default (off the main thread)
     * since image rendering, while fast, still touches disk I/O for the
     * PNG write -- not something to do inline on the caller's dispatcher.
     * Returns null if there's no verse loaded or generation fails, letting
     * the caller (HomeScreen) skip launching a share sheet with nothing to
     * share rather than crashing.
     */
    suspend fun generateShareImage(): Uri? {
        val daily = _uiState.value.dailyVerse ?: return null
        val religionId = _uiState.value.religion?.id ?: return null
        return withContext(Dispatchers.Default) {
            runCatching {
                shareImageGenerator.generate(daily.verse.translatedText, daily.verse.sourceReference, religionId)
            }.getOrNull()
        }
    }

    override fun onCleared() {
        ttsManager.shutdown()
        super.onCleared()
    }
}
