package com.dailydivine.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.alarm.AlarmScheduler
import com.dailydivine.app.data.content.ContentMigrationManager
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.util.ReligionMeta
import com.dailydivine.app.util.Religions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val selectedReligionId: Int? = null,
    val selectedLanguageCode: String = "en",
    val alarmHour: Int = 5,
    val alarmMinute: Int = 30,
    val ttsEnabled: Boolean = true,
    val canScheduleExactAlarms: Boolean = true,
    val isCompleting: Boolean = false
) {
    val selectedReligion: ReligionMeta?
        get() = selectedReligionId?.let { Religions.byId(it) }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val contentMigrationManager: ContentMigrationManager,
    @ApplicationContext private val appContext: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            canScheduleExactAlarms = AlarmScheduler(appContext).canScheduleExactAlarms()
        )
    }

    fun selectReligion(religionId: Int) {
        val religion = Religions.byId(religionId)
        _uiState.value = _uiState.value.copy(
            selectedReligionId = religionId,
            // Default to the religion's first supported language rather than
            // leaving a stale selection from a previously-viewed religion.
            selectedLanguageCode = religion?.languages?.firstOrNull()?.second ?: "en"
        )
    }

    fun selectLanguage(languageCode: String) {
        _uiState.value = _uiState.value.copy(selectedLanguageCode = languageCode)
    }

    fun setTtsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(ttsEnabled = enabled)
    }

    fun refreshExactAlarmPermission() {
        _uiState.value = _uiState.value.copy(
            canScheduleExactAlarms = AlarmScheduler(appContext).canScheduleExactAlarms()
        )
    }

    /**
     * Called from the Permission screen (S06). Persists everything collected
     * across onboarding, stamps the install date (F002's day-number anchor),
     * and kicks off the first content load for the selected religion via the
     * v1.1 Content Migration Manager (F002-R13) — the same code path an app
     * update will use later, so this is exercised from day one rather than
     * only in a hypothetical future update.
     */
    fun completeOnboarding(notificationsGranted: Boolean, onDone: () -> Unit) {
        val religionId = _uiState.value.selectedReligionId ?: Religions.ALL.first().id
        val languageCode = _uiState.value.selectedLanguageCode
        val religion = Religions.byId(religionId)

        _uiState.value = _uiState.value.copy(isCompleting = true)

        viewModelScope.launch {
            userPreferences.setReligion(religionId)
            userPreferences.setLanguage(languageCode)
            userPreferences.setNotificationsGranted(notificationsGranted)
            userPreferences.completeOnboarding()

            // Only Hinduism has a real sample content file as of this sprint
            // (see util/Religions.kt) -- other religions simply have no
            // verses yet, which HomeScreen already handles gracefully
            // (shows "No verse available yet" rather than crashing).
            religion?.contentAssetEn?.let { assetFile ->
                runCatching {
                    contentMigrationManager.migrateIfNeeded(religionId, assetFile, "en")
                }
                // Deliberately swallow failures here rather than block
                // onboarding completion on content loading -- a missing/bad
                // JSON file shouldn't strand the user on the permission
                // screen. HomeScreen's empty-state handles zero verses fine.
            }

            _uiState.value = _uiState.value.copy(isCompleting = false)
            onDone()
        }
    }
}
