package com.dailydivine.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
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
    val notificationsGranted: Boolean = false,
    // F009 notification settings (R01/R02 morning verse, R06-R09 streak reminder, R10 milestones, R13 toggles)
    val dailyVerseEnabled: Boolean = true,
    val dailyVerseHour: Int = 7,    // F009-R02 default 7:00 AM
    val dailyVerseMinute: Int = 0,
    val streakReminderEnabled: Boolean = true,
    val milestonesEnabled: Boolean = true,
    val lastMilestoneDays: Int = 0, // highest milestone already celebrated, so each fires once
    val ttsRate: Float = 1.0f,      // F012-R06 voice speed: 0.75 slow, 1.0 normal, 1.25 fast
    // Daily wallpaper (new feature): OFF by default, so nothing ever changes without consent.
    val wallpaperTarget: String = "OFF",          // OFF | HOME | LOCK | BOTH
    val wallpaperUseCustom: Boolean = false,      // true = user's own photo, false = religion gradient
    val wallpaperHour: Int = 5,                   // daily update time (5:00 AM)
    val wallpaperMinute: Int = 0,
    val wallpaperTextPosition: String = "MIDDLE", // UPPER | MIDDLE | LOWER
    val wallpaperLastEpochDay: Long = -1L,        // day today's wallpaper was last applied
    // Diagnostics: when/why the wallpaper job last ran, so "it only updates when I open the app"
    // can be answered from the app itself (Wallpaper screen > "Last check").
    val wallpaperLastAttemptMillis: Long = 0L,
    val wallpaperLastAttemptSource: String = "",  // ALARM | WORK | APP_OPEN | MANUAL
    val wallpaperLastAttemptResult: String = "",  // APPLIED | UP_TO_DATE | DISABLED | NO_VERSE | FAILED
    // Idempotence: the alarm and the WorkManager backup can both fire; post each notification once a day.
    val lastVerseNotifEpochDay: Long = -1L,
    val lastStreakNotifEpochDay: Long = -1L
)

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val RELIGION_ID = intPreferencesKey("religion_id")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val INSTALL_EPOCH_DAY = longPreferencesKey("install_epoch_day")
        val NOTIFICATIONS_GRANTED = booleanPreferencesKey("notifications_granted")
        val DAILY_VERSE_ENABLED = booleanPreferencesKey("daily_verse_enabled")
        val DAILY_VERSE_HOUR = intPreferencesKey("daily_verse_hour")
        val DAILY_VERSE_MINUTE = intPreferencesKey("daily_verse_minute")
        val STREAK_REMINDER_ENABLED = booleanPreferencesKey("streak_reminder_enabled")
        val MILESTONES_ENABLED = booleanPreferencesKey("milestones_enabled")
        val LAST_MILESTONE_DAYS = intPreferencesKey("last_milestone_days")
        val TTS_RATE = floatPreferencesKey("tts_rate")
        val WALLPAPER_TARGET = stringPreferencesKey("wallpaper_target")
        val WALLPAPER_USE_CUSTOM = booleanPreferencesKey("wallpaper_use_custom")
        val WALLPAPER_HOUR = intPreferencesKey("wallpaper_hour")
        val WALLPAPER_MINUTE = intPreferencesKey("wallpaper_minute")
        val WALLPAPER_TEXT_POSITION = stringPreferencesKey("wallpaper_text_position")
        val WALLPAPER_LAST_EPOCH_DAY = longPreferencesKey("wallpaper_last_epoch_day")
        val WALLPAPER_LAST_ATTEMPT_MILLIS = longPreferencesKey("wallpaper_last_attempt_millis")
        val WALLPAPER_LAST_ATTEMPT_SOURCE = stringPreferencesKey("wallpaper_last_attempt_source")
        val WALLPAPER_LAST_ATTEMPT_RESULT = stringPreferencesKey("wallpaper_last_attempt_result")
        val LAST_VERSE_NOTIF_EPOCH_DAY = longPreferencesKey("last_verse_notif_epoch_day")
        val LAST_STREAK_NOTIF_EPOCH_DAY = longPreferencesKey("last_streak_notif_epoch_day")
    }

    val state: Flow<UserPrefsState> = context.dataStore.data.map { prefs ->
        UserPrefsState(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            religionId = prefs[Keys.RELIGION_ID],
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            installEpochDay = prefs[Keys.INSTALL_EPOCH_DAY],
            notificationsGranted = prefs[Keys.NOTIFICATIONS_GRANTED] ?: false,
            dailyVerseEnabled = prefs[Keys.DAILY_VERSE_ENABLED] ?: true,
            dailyVerseHour = prefs[Keys.DAILY_VERSE_HOUR] ?: 7,
            dailyVerseMinute = prefs[Keys.DAILY_VERSE_MINUTE] ?: 0,
            streakReminderEnabled = prefs[Keys.STREAK_REMINDER_ENABLED] ?: true,
            milestonesEnabled = prefs[Keys.MILESTONES_ENABLED] ?: true,
            lastMilestoneDays = prefs[Keys.LAST_MILESTONE_DAYS] ?: 0,
            ttsRate = prefs[Keys.TTS_RATE] ?: 1.0f,
            wallpaperTarget = prefs[Keys.WALLPAPER_TARGET] ?: "OFF",
            wallpaperUseCustom = prefs[Keys.WALLPAPER_USE_CUSTOM] ?: false,
            wallpaperHour = prefs[Keys.WALLPAPER_HOUR] ?: 5,
            wallpaperMinute = prefs[Keys.WALLPAPER_MINUTE] ?: 0,
            wallpaperTextPosition = prefs[Keys.WALLPAPER_TEXT_POSITION] ?: "MIDDLE",
            wallpaperLastEpochDay = prefs[Keys.WALLPAPER_LAST_EPOCH_DAY] ?: -1L,
            wallpaperLastAttemptMillis = prefs[Keys.WALLPAPER_LAST_ATTEMPT_MILLIS] ?: 0L,
            wallpaperLastAttemptSource = prefs[Keys.WALLPAPER_LAST_ATTEMPT_SOURCE] ?: "",
            wallpaperLastAttemptResult = prefs[Keys.WALLPAPER_LAST_ATTEMPT_RESULT] ?: "",
            lastVerseNotifEpochDay = prefs[Keys.LAST_VERSE_NOTIF_EPOCH_DAY] ?: -1L,
            lastStreakNotifEpochDay = prefs[Keys.LAST_STREAK_NOTIF_EPOCH_DAY] ?: -1L
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

    suspend fun setDailyVerseEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_VERSE_ENABLED] = enabled }
    }

    suspend fun setDailyVerseTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.DAILY_VERSE_HOUR] = hour
            it[Keys.DAILY_VERSE_MINUTE] = minute
        }
    }

    suspend fun setStreakReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STREAK_REMINDER_ENABLED] = enabled }
    }

    suspend fun setMilestonesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MILESTONES_ENABLED] = enabled }
    }

    suspend fun setWallpaperTarget(name: String) { context.dataStore.edit { it[Keys.WALLPAPER_TARGET] = name } }
    suspend fun setWallpaperUseCustom(value: Boolean) { context.dataStore.edit { it[Keys.WALLPAPER_USE_CUSTOM] = value } }
    suspend fun setWallpaperTime(hour: Int, minute: Int) {
        context.dataStore.edit { it[Keys.WALLPAPER_HOUR] = hour; it[Keys.WALLPAPER_MINUTE] = minute }
    }
    suspend fun setWallpaperTextPosition(name: String) { context.dataStore.edit { it[Keys.WALLPAPER_TEXT_POSITION] = name } }
    suspend fun setWallpaperLastAttempt(millis: Long, source: String, result: String) {
        context.dataStore.edit {
            it[Keys.WALLPAPER_LAST_ATTEMPT_MILLIS] = millis
            it[Keys.WALLPAPER_LAST_ATTEMPT_SOURCE] = source
            it[Keys.WALLPAPER_LAST_ATTEMPT_RESULT] = result
        }
    }
    suspend fun setLastVerseNotifEpochDay(day: Long) { context.dataStore.edit { it[Keys.LAST_VERSE_NOTIF_EPOCH_DAY] = day } }
    suspend fun setLastStreakNotifEpochDay(day: Long) { context.dataStore.edit { it[Keys.LAST_STREAK_NOTIF_EPOCH_DAY] = day } }
    suspend fun setWallpaperLastEpochDay(day: Long) { context.dataStore.edit { it[Keys.WALLPAPER_LAST_EPOCH_DAY] = day } }

    suspend fun setTtsRate(rate: Float) {
        context.dataStore.edit { it[Keys.TTS_RATE] = rate }
    }

    suspend fun setLastMilestoneDays(days: Int) {
        context.dataStore.edit { it[Keys.LAST_MILESTONE_DAYS] = days }
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
