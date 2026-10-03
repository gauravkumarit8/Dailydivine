package com.dailydivine.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

private const val TAG = "NotificationWorkers"

private fun entryPoint(context: Context): NotificationEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, NotificationEntryPoint::class.java)

/**
 * F009-R01..R05: morning verse notification. Always re-arms tomorrow's run in
 * `finally`, so an exception today can never silently end the daily chain.
 */
class DailyVerseWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ep = entryPoint(applicationContext)
        val prefs = ep.userPreferences().state.first()
        val active = prefs.onboardingCompleted && prefs.dailyVerseEnabled
        try {
            if (active && prefs.religionId != null) {
                val installDate = prefs.installEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
                val daily = ep.verseRepository().getDailyVerse(prefs.religionId, installDate, LocalDate.now())
                daily?.let {
                    NotificationHelper.postDailyVerse(applicationContext, it.verse.translatedText, it.verse.sourceReference)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Daily verse notification failed", e)
        } finally {
            if (active) {
                NotificationScheduler(applicationContext)
                    .scheduleDailyVerse(prefs.dailyVerseHour, prefs.dailyVerseMinute, replace = true)
            }
        }
        return Result.success()
    }
}

/**
 * F009-R06..R09: 8 PM streak reminder. Sent only if the app has NOT been
 * opened today (R08) and the streak going into today is above 3 days (R09).
 */
class StreakReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ep = entryPoint(applicationContext)
        val prefs = ep.userPreferences().state.first()
        val active = prefs.onboardingCompleted && prefs.streakReminderEnabled
        try {
            if (active && !ep.streakRepository().hasOpenedToday()) {
                val streak = ep.streakRepository().streakEndingYesterday()
                if (streak > 3) NotificationHelper.postStreakReminder(applicationContext, streak)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Streak reminder failed", e)
        } finally {
            if (active) NotificationScheduler(applicationContext).scheduleStreakReminder(replace = true)
        }
        return Result.success()
    }
}
