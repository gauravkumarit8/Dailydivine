package com.dailydivine.app.wallpaper

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dailydivine.app.notifications.NotificationScheduler
import java.util.concurrent.TimeUnit

/**
 * Daily wallpaper job, same self-renewing one-time-work pattern as the notifications (see
 * NotificationScheduler for why not PeriodicWork). Delivery is inexact (Doze can delay it),
 * so the app also catches up whenever it is opened (HomeViewModel.refresh).
 */
class WallpaperScheduler(context: Context) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun schedule(hour: Int, minute: Int, replace: Boolean) {
        val request = OneTimeWorkRequest.Builder(WallpaperWorker::class.java)
            .setInitialDelay(NotificationScheduler.millisUntilNext(hour, minute), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            UNIQUE_NAME, if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP, request
        )
    }

    fun cancel() { workManager.cancelUniqueWork(UNIQUE_NAME) }

    companion object { const val UNIQUE_NAME = "wallpaper_daily" }
}
