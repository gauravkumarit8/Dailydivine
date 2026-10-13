package com.dailydivine.app.wallpaper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dailydivine.app.jobs.DailyAlarmScheduler
import com.dailydivine.app.jobs.DailyJob
import com.dailydivine.app.jobs.planFor
import com.dailydivine.app.notifications.NotificationEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

/**
 * BACKUP trigger for the daily wallpaper (the primary one is the Doze-proof alarm in
 * jobs/DailyAlarmScheduler). Applies the day's wallpaper (a no-op if the alarm already did),
 * then always re-arms tomorrow's run and the alarm if it is missing.
 */
class WallpaperWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ep = EntryPointAccessors.fromApplication(applicationContext, NotificationEntryPoint::class.java)
        val prefs = ep.userPreferences().state.first()
        val plan = planFor(DailyJob.WALLPAPER, prefs)
        try {
            if (plan.active) ep.wallpaperUpdater().updateIfNeeded(WallpaperUpdater.Trigger.WORK)
        } catch (e: Exception) {
            Log.e("WallpaperWorker", "Daily wallpaper update failed", e)
        } finally {
            if (plan.active) {
                WallpaperScheduler(applicationContext).schedule(plan.hour, plan.minute, replace = true)
                DailyAlarmScheduler(applicationContext).schedule(DailyJob.WALLPAPER, plan.hour, plan.minute, replace = true)
            }
        }
        return Result.success()
    }
}
