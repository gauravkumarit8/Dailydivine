package com.dailydivine.app.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the two daily notification jobs as self-renewing one-time work:
 * each worker re-enqueues tomorrow's run as its last step. One-time work with
 * a computed delay (rather than PeriodicWork) is used because periodic work
 * can drift anywhere inside its flex window, and these are time-of-day
 * notifications. WorkManager survives reboots, so no BootReceiver is needed.
 * Delivery is inexact (Doze may delay by minutes), which is fine for these.
 */
class NotificationScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    /** @param replace true to restart the timer (settings changed, or a worker
     *  renewing itself); false (KEEP) on plain app launch so a pending run is
     *  not pushed back every time the app opens, while a dead chain is revived. */
    fun scheduleDailyVerse(hour: Int, minute: Int, replace: Boolean) {
        enqueue(UNIQUE_DAILY_VERSE, DailyVerseWorker::class.java, hour, minute, replace)
    }

    /** F009-R06: 8:00 PM. */
    fun scheduleStreakReminder(replace: Boolean) {
        enqueue(UNIQUE_STREAK_REMINDER, StreakReminderWorker::class.java, 20, 0, replace)
    }

    fun cancelDailyVerse() { workManager.cancelUniqueWork(UNIQUE_DAILY_VERSE) }
    fun cancelStreakReminder() { workManager.cancelUniqueWork(UNIQUE_STREAK_REMINDER) }
    fun cancelAll() { cancelDailyVerse(); cancelStreakReminder() }

    private fun enqueue(
        uniqueName: String, worker: Class<out ListenableWorker>, hour: Int, minute: Int, replace: Boolean
    ) {
        val request = OneTimeWorkRequest.Builder(worker)
            .setInitialDelay(millisUntilNext(hour, minute), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            uniqueName,
            if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val UNIQUE_DAILY_VERSE = "notif_daily_verse"
        const val UNIQUE_STREAK_REMINDER = "notif_streak_reminder"

        /** Milliseconds until the next [hour]:[minute] strictly after [now]. */
        internal fun millisUntilNext(hour: Int, minute: Int, now: LocalDateTime = LocalDateTime.now()): Long {
            var next = now.toLocalDate().atTime(hour, minute)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return Duration.between(now, next).toMillis()
        }
    }
}
