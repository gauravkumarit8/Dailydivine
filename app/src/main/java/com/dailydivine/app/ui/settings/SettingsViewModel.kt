package com.dailydivine.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.content.ContentMigrationManager
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.local.datastore.UserPrefsState
import com.dailydivine.app.data.local.db.AppDatabase
import com.dailydivine.app.data.repository.AlarmRepository
import com.dailydivine.app.util.ReligionMeta
import com.dailydivine.app.util.Religions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val alarmRepository: AlarmRepository,
    private val appDatabase: AppDatabase,
    private val contentMigrationManager: ContentMigrationManager
) : ViewModel() {

    private val _religion = MutableStateFlow<ReligionMeta?>(null)
    val religion: StateFlow<ReligionMeta?> = _religion.asStateFlow()

    private val _languageCode = MutableStateFlow("en")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    private val _prefs = MutableStateFlow(UserPrefsState())
    /** Full prefs snapshot, for the Notifications section's switches and time. */
    val prefs: StateFlow<UserPrefsState> = _prefs.asStateFlow()

    fun setDailyVerseEnabled(enabled: Boolean) { viewModelScope.launch { userPreferences.setDailyVerseEnabled(enabled) } }
    fun setDailyVerseTime(hour: Int, minute: Int) { viewModelScope.launch { userPreferences.setDailyVerseTime(hour, minute) } }
    fun setStreakReminderEnabled(enabled: Boolean) { viewModelScope.launch { userPreferences.setStreakReminderEnabled(enabled) } }
    fun setTtsRate(rate: Float) { viewModelScope.launch { userPreferences.setTtsRate(rate) } }
    fun setMilestonesEnabled(enabled: Boolean) { viewModelScope.launch { userPreferences.setMilestonesEnabled(enabled) } }

    init {
        viewModelScope.launch {
            userPreferences.state.collect { prefs ->
                _prefs.value = prefs
                _religion.value = prefs.religionId?.let { Religions.byId(it) }
                _languageCode.value = prefs.languageCode
            }
        }
    }

    /**
     * F001-R04: change religion after onboarding. The new religion's content
     * is loaded BEFORE the preference is persisted: Home/Library react to the
     * preference change immediately, so persisting first would make them
     * query an empty table and flash the "no verses" state.
     * Streaks, bookmarks and alarms are untouched (never deleted).
     */
    fun changeReligion(religionId: Int) {
        viewModelScope.launch {
            Religions.byId(religionId)?.contentAssetEn?.let { asset ->
                runCatching { contentMigrationManager.migrateIfNeeded(religionId, asset, "en") }
            }
            userPreferences.setReligion(religionId)
        }
    }

    /**
     * F012-R16: "Clear all data (with confirmation)" -- confirmation itself
     * lives in SettingsScreen's AlertDialog, this performs the actual clear.
     *
     * Order matters: alarms must be cancelled via AlarmRepository.delete()
     * (which calls AlarmScheduler.cancel() before removing the Room row)
     * WHILE we still have their IDs. Clearing Room first would leave any
     * scheduled AlarmManager entries orphaned with no way to reach them
     * afterward -- previously this method only cleared DataStore, silently
     * leaving Room (streaks/bookmarks) and any real scheduled alarm intact
     * despite the button's name. Fixed this session.
     */
    fun clearAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            alarmRepository.getAllAlarms().first().forEach { alarmRepository.delete(it) }
            appDatabase.clearAllTables()
            userPreferences.clearAll()
            onDone()
        }
    }
}
