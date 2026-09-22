package com.dailydivine.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.util.ReligionMeta
import com.dailydivine.app.util.Religions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
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
     * lives in SettingsScreen's AlertDialog, this just performs the clear.
     *
     * NOTE: currently clears DataStore (onboarding/religion/language) only.
     * Room data (streaks, bookmarks, scheduled alarms) is NOT yet wiped --
     * a true full reset also needs AppDatabase.clearAllTables() and
     * cancelling any scheduled alarms via AlarmRepository first. Tracked as
     * a follow-up in CHECKLIST.md rather than silently claiming this does
     * more than it does.
     */
    fun clearAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            userPreferences.clearAll()
            onDone()
        }
    }
}
