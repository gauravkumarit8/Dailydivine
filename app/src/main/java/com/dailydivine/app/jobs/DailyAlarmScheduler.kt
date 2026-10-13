package com.dailydivine.app.jobs

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dailydivine.app.notifications.NotificationScheduler

/**
 * Triggers the daily jobs with AlarmManager.setAndAllowWhileIdle, which Android delivers
 * even while the phone is in Doze overnight (needs no special permission).
 *
 * WHY NOT ONLY WORKMANAGER: a job with a many-hours initial delay is just another background
 * job: on an idle phone Android runs it only in a maintenance window, which can be hours
 * after the requested time, or when the user next wakes the phone. That is exactly the
 * reported "the wallpaper only changes once I open the app". The user's alarms never had the
 * problem because they use setAlarmClock, the one call Doze exempts. WorkManager stays as a
 * backup (the jobs are idempotent per day), but this is the primary trigger.
 */
class DailyAlarmScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Arms the next occurrence of [hour]:[minute]. Re-arming is always safe: the trigger is an
     * absolute wall-clock time, so setting it again just yields the same moment.
     *
     * [replace] = false skips the call when a PendingIntent for the job exists. NOTE the limit:
     * a PendingIntent survives after its alarm has FIRED, so "exists" does not mean "an alarm is
     * pending"; callers that must revive a possibly-dead chain (app launch, boot) pass true.
     */
    fun schedule(job: DailyJob, hour: Int, minute: Int, replace: Boolean) {
        if (!replace && existing(job) != null) return
        // Never closer than MIN_DELAY_MS: a job that fires and re-arms must not be able to schedule
        // itself for "right now" and spin.
        val delay = NotificationScheduler.millisUntilNext(hour, minute).coerceAtLeast(MIN_DELAY_MS)
        val triggerAt = System.currentTimeMillis() + delay
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(job))
    }

    fun cancel(job: DailyJob) {
        existing(job)?.let { alarmManager.cancel(it); it.cancel() }
    }

    /** Arms every active job and cancels the rest. */
    fun sync(plans: Map<DailyJob, JobPlan>, replace: Boolean) {
        plans.forEach { (job, plan) ->
            if (plan.active) schedule(job, plan.hour, plan.minute, replace) else cancel(job)
        }
    }

    private fun intentFor(job: DailyJob) =
        Intent(appContext, DailyJobReceiver::class.java).putExtra(EXTRA_JOB, job.name)

    private fun pendingIntent(job: DailyJob): PendingIntent =
        PendingIntent.getBroadcast(
            appContext, job.requestCode, intentFor(job),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun existing(job: DailyJob): PendingIntent? =
        PendingIntent.getBroadcast(
            appContext, job.requestCode, intentFor(job),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

    companion object {
        const val EXTRA_JOB = "daily_job"
        private const val MIN_DELAY_MS = 30_000L
    }
}
