package com.dailydivine.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dailydivine.app.jobs.DailyAlarmScheduler
import com.dailydivine.app.jobs.DailyJob
import com.dailydivine.app.jobs.planFor
import com.dailydivine.app.jobs.runDailyVerse
import com.dailydivine.app.jobs.runStreakReminder
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

private const val TAG = "NotificationWorkers"

private fun entryPoint(context: Context): NotificationEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, NotificationEntryPoint::class.java)

/**
 * BACKUP trigger for the morning verse notification (the primary one is the Doze-proof alarm in
 * jobs/DailyAlarmScheduler). Always re-arms tomorrow's run in `finally`, and also re-arms the alarm
 * if it is missing, so either mechanism can revive the other.
 */
class DailyVerseWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ep = entryPoint(applicationContext)
        val prefs = ep.userPreferences().state.first()
        val plan = planFor(DailyJob.DAILY_VERSE, prefs)
        try {
            runDailyVerse(applicationContext, prefs, ep.userPreferences(), ep.verseRepository())
        } catch (e: Exception) {
            Log.e(TAG, "Daily verse notification failed", e)
        } finally {
            if (plan.active) {
                NotificationScheduler(applicationContext).scheduleDailyVerse(plan.hour, plan.minute, replace = true)
                DailyAlarmScheduler(applicationContext).schedule(DailyJob.DAILY_VERSE, plan.hour, plan.minute, replace = true)
            }
        }
        return Result.success()
    }
}

/** BACKUP trigger for the 8 PM streak reminder; see [DailyVerseWorker]. */
class StreakReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ep = entryPoint(applicationContext)
        val prefs = ep.userPreferences().state.first()
        val plan = planFor(DailyJob.STREAK_REMINDER, prefs)
        try {
            runStreakReminder(applicationContext, prefs, ep.userPreferences(), ep.streakRepository())
        } catch (e: Exception) {
            Log.e(TAG, "Streak reminder failed", e)
        } finally {
            if (plan.active) {
                NotificationScheduler(applicationContext).scheduleStreakReminder(replace = true)
                DailyAlarmScheduler(applicationContext).schedule(DailyJob.STREAK_REMINDER, plan.hour, plan.minute, replace = true)
            }
        }
        return Result.success()
    }
}
