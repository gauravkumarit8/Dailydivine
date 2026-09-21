package com.dailydivine.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "dailydivine_prefs")

/**
 * Backs F001-R07 ("All selections MUST be stored in local SharedPreferences" —
 * implemented with the modern DataStore equivalent instead) and closes the
 * Sprint 2 gap where onboarding selections weren't persisted anywhere.
 */
data class UserPrefsState(
    val onboardingCompleted: Boolean = false,
    val religionId: Int? = null,
    val languageCode: String = "en",
    val installEpochDay: Long? = null, // anchors the daily-verse day-number algorithm (API Contract 1)
    val notificationsGranted: Boolean = false
)

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val RELIGION_ID = intPreferencesKey("religion_id")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val INSTALL_EPOCH_DAY = longPreferencesKey("install_epoch_day")
        val NOTIFICATIONS_GRANTED = booleanPreferencesKey("notifications_granted")
    }

    val state: Flow<UserPrefsState> = context.dataStore.data.map { prefs ->
        UserPrefsState(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            religionId = prefs[Keys.RELIGION_ID],
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            installEpochDay = prefs[Keys.INSTALL_EPOCH_DAY],
            notificationsGranted = prefs[Keys.NOTIFICATIONS_GRANTED] ?: false
        )
    }

    suspend fun setReligion(religionId: Int) {
        context.dataStore.edit { it[Keys.RELIGION_ID] = religionId }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { it[Keys.LANGUAGE_CODE] = languageCode }
    }

    suspend fun setNotificationsGranted(granted: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_GRANTED] = granted }
    }

    /** Called once, on completing onboarding. Also stamps the install date
     *  if this is genuinely the first time (idempotent — never overwrites
     *  an existing install date, since that would break the deterministic
     *  daily-verse day-number algorithm for a returning user). */
    suspend fun completeOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
            if (prefs[Keys.INSTALL_EPOCH_DAY] == null) {
                prefs[Keys.INSTALL_EPOCH_DAY] = LocalDate.now().toEpochDay()
            }
        }
    }

    /** F012-R16: "Clear all data (with confirmation)" — Settings screen, Sprint 7+. */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
