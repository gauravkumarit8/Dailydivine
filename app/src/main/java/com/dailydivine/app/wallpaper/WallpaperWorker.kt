package com.dailydivine.app.wallpaper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dailydivine.app.notifications.NotificationEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

/** Applies the day's wallpaper, then always re-arms tomorrow's run (even after a failure). */
class WallpaperWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ep = EntryPointAccessors.fromApplication(applicationContext, NotificationEntryPoint::class.java)
        val prefs = ep.userPreferences().state.first()
        val active = prefs.onboardingCompleted && WallpaperTarget.fromName(prefs.wallpaperTarget) != WallpaperTarget.OFF
        try {
            if (active) ep.wallpaperUpdater().updateIfNeeded()
        } catch (e: Exception) {
            Log.e("WallpaperWorker", "Daily wallpaper update failed", e)
        } finally {
            if (active) WallpaperScheduler(applicationContext).schedule(prefs.wallpaperHour, prefs.wallpaperMinute, replace = true)
        }
        return Result.success()
    }
}
