package com.dailydivine.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.datastore.UserPreferences
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
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val _religion = MutableStateFlow<ReligionMeta?>(null)
    val religion: StateFlow<ReligionMeta?> = _religion.asStateFlow()

    private val _languageCode = MutableStateFlow("en")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.state.collect { prefs ->
                _religion.value = prefs.religionId?.let { Religions.byId(it) }
                _languageCode.value = prefs.languageCode
            }
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
