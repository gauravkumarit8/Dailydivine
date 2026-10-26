package com.dailydivine.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.content.ContentMigrationManager
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.AlarmRepository
import com.dailydivine.app.jobs.DailyAlarmScheduler
import com.dailydivine.app.jobs.plansFor
import com.dailydivine.app.notifications.NotificationScheduler
import com.dailydivine.app.wallpaper.WallpaperScheduler
import com.dailydivine.app.wallpaper.WallpaperTarget
import com.dailydivine.app.util.Religions
import com.dailydivine.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val contentMigrationManager: ContentMigrationManager,
    private val alarmRepository: AlarmRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    /** Null while still resolving -- MainActivity keeps the splash screen up
     *  until this becomes non-null, so NavHost is only ever composed once
     *  the real start destination is known (never flashes Welcome before
     *  jumping to Home for a returning user, or vice versa). */
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    /**
     * BUG FIX: MainActivity previously called DailyDivineTheme { ... } with
     * no religionId at all, which silently defaults to 0 -- the Spiritual
     * (teal) palette -- for every single user, regardless of which of the
     * 7 religions they actually selected. The whole point of having 7
     * distinct ReligionPalette values (ui/theme/Color.kt) was to visually
     * theme the app per-religion; that never actually happened anywhere.
     * Reactively observed (not a one-shot read) so the app re-themes itself
     * immediately if the user changes religion from Settings, without
     * needing a restart.
     */
    private val _currentReligionId = MutableStateFlow<Int?>(null)
    val currentReligionId: StateFlow<Int?> = _currentReligionId.asStateFlow()

    /** The subset of prefs that decides what notification work should exist. */
    private data class NotifKey(
        val onboarded: Boolean, val dailyVerse: Boolean, val hour: Int, val minute: Int, val streakReminder: Boolean
    )

    init {
        // A PendingIntent that still exists is not proof the alarm is armed (it survives after
        // firing), and alarms armed through the inexact fallback stay inexact after the user
        // grants exact alarms. Re-arming every enabled alarm on each app start is idempotent
        // and closes both gaps. Failures must never block startup.
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { alarmRepository.rescheduleAllEnabled() }
        }
        // F009: keep scheduled notification work in sync with the settings.
        // First emission (app launch) uses KEEP so opening the app never
        // pushes a pending run back; later emissions are real setting
        // changes (or onboarding finishing) and restart the timer. Clearing
        // all data flips onboarded to false, which cancels everything.
        viewModelScope.launch {
            val scheduler = NotificationScheduler(appContext)
            var first = true
            userPreferences.state
                .map { NotifKey(it.onboardingCompleted, it.dailyVerseEnabled, it.dailyVerseHour, it.dailyVerseMinute, it.streakReminderEnabled) }
                .distinctUntilChanged()
                .collect { k ->
                    val replace = !first
                    first = false
                    if (!k.onboarded) {
                        scheduler.cancelAll()
                    } else {
                        if (k.dailyVerse) scheduler.scheduleDailyVerse(k.hour, k.minute, replace) else scheduler.cancelDailyVerse()
                        if (k.streakReminder) scheduler.scheduleStreakReminder(replace) else scheduler.cancelStreakReminder()
                    }
                }
        }

        // PRIMARY trigger for all three daily jobs: Doze-proof alarms (the WorkManager chains
        // above and below are the backup). Armed only while a job is on. Always re-armed (also on
        // every app launch): a fired alarm leaves its PendingIntent behind, so "already exists"
        // cannot tell a live chain from a dead one, and re-setting the same wall-clock time is harmless.
        viewModelScope.launch {
            val alarms = DailyAlarmScheduler(appContext)
            userPreferences.state
                .map { plansFor(it) }
                .distinctUntilChanged()
                .collect { plans -> alarms.sync(plans, replace = true) }
        }

        // Daily wallpaper: schedule only while it is turned on (and onboarding is done).
        // Same KEEP-on-launch / REPLACE-on-change rule as the notifications above.
        viewModelScope.launch {
            val scheduler = WallpaperScheduler(appContext)
            var first = true
            userPreferences.state
                .map { Triple(it.onboardingCompleted && WallpaperTarget.fromName(it.wallpaperTarget) != WallpaperTarget.OFF, it.wallpaperHour, it.wallpaperMinute) }
                .distinctUntilChanged()
                .collect { (active, hour, minute) ->
                    val replace = !first
                    first = false
                    if (active) scheduler.schedule(hour, minute, replace) else scheduler.cancel()
                }
        }

        // F002-R13: apply bundled content updates on every launch. Previously
        // migrateIfNeeded ran only once during onboarding, so a shipped
        // content update never reached existing users. It is a cheap no-op
        // when the stored content version is already current.
        viewModelScope.launch {
            userPreferences.state
                .map { it.religionId }
                .filterNotNull()
                .distinctUntilChanged()
                .collect { id ->
                    Religions.byId(id)?.contentAssetEn?.let { asset ->
                        runCatching { contentMigrationManager.migrateIfNeeded(id, asset, "en") }
                    }
                }
        }
        viewModelScope.launch {
            userPreferences.state.collect { prefs -> _currentReligionId.value = prefs.religionId }
        }
        viewModelScope.launch {
            val prefs = userPreferences.state.first()
            _startDestination.value = if (prefs.onboardingCompleted) {
                Screen.Home.route
            } else {
                // Must be the nested graph's own route, not Welcome.route
                // directly -- same bug/fix as NavGraph.kt's default
                // startDestination, see Screen.kt's OnboardingGraph comment.
                Screen.OnboardingGraph.route
            }
        }
    }
}
