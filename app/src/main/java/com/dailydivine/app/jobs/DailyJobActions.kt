package com.dailydivine.app.jobs

import android.content.Context
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.local.datastore.UserPrefsState
import com.dailydivine.app.data.repository.StreakRepository
import com.dailydivine.app.data.repository.VerseRepository
import com.dailydivine.app.notifications.NotificationHelper
import java.time.LocalDate

/*
 * The actual work of the daily notification jobs, shared by the alarm receiver (primary trigger)
 * and the WorkManager workers (backup). Both can fire on the same day, so each action records the
 * day it last posted and does nothing a second time: the user must never get two "Today's verse"
 * notifications.
 */

/** F009-R01..R05: the morning verse notification. */
internal suspend fun runDailyVerse(
    context: Context, prefs: UserPrefsState, userPreferences: UserPreferences, verseRepository: VerseRepository
) {
    if (!prefs.onboardingCompleted || !prefs.dailyVerseEnabled) return
    val today = LocalDate.now().toEpochDay()
    if (prefs.lastVerseNotifEpochDay == today) return
    val religionId = prefs.religionId ?: return
    val installDate = prefs.installEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
    val daily = verseRepository.getDailyVerse(religionId, installDate, LocalDate.now()) ?: return
    NotificationHelper.postDailyVerse(context, daily.verse.translatedText, daily.verse.sourceReference)
    userPreferences.setLastVerseNotifEpochDay(today)
}

/** F009-R06..R09: 8 PM, only if today wasn't opened yet and the streak into today is over 3 days. */
internal suspend fun runStreakReminder(
    context: Context, prefs: UserPrefsState, userPreferences: UserPreferences, streakRepository: StreakRepository
) {
    if (!prefs.onboardingCompleted || !prefs.streakReminderEnabled) return
    val today = LocalDate.now().toEpochDay()
    if (prefs.lastStreakNotifEpochDay == today) return
    if (streakRepository.hasOpenedToday()) return
    val streak = streakRepository.streakEndingYesterday()
    if (streak > 3) {
        NotificationHelper.postStreakReminder(context, streak)
        userPreferences.setLastStreakNotifEpochDay(today)
    }
}
