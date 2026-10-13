package com.dailydivine.app.jobs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.StreakRepository
import com.dailydivine.app.data.repository.VerseRepository
import com.dailydivine.app.wallpaper.WallpaperUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fired by [DailyAlarmScheduler] (works in Doze). Runs the job, then ALWAYS arms tomorrow's alarm,
 * so one failure can never end the daily chain. goAsync keeps the process alive for the work.
 */
@AndroidEntryPoint
class DailyJobReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var verseRepository: VerseRepository
    @Inject lateinit var streakRepository: StreakRepository
    @Inject lateinit var wallpaperUpdater: WallpaperUpdater

    override fun onReceive(context: Context, intent: Intent) {
        val job = DailyJob.fromName(intent.getStringExtra(DailyAlarmScheduler.EXTRA_JOB)) ?: return
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val prefs = userPreferences.state.first()
                if (planFor(job, prefs).active) {
                    when (job) {
                        DailyJob.WALLPAPER -> wallpaperUpdater.updateIfNeeded(WallpaperUpdater.Trigger.ALARM)
                        DailyJob.DAILY_VERSE -> runDailyVerse(appContext, prefs, userPreferences, verseRepository)
                        DailyJob.STREAK_REMINDER -> runStreakReminder(appContext, prefs, userPreferences, streakRepository)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Daily job $job failed", e)
            } finally {
                try {
                    val fresh = userPreferences.state.first()
                    val plan = planFor(job, fresh)
                    if (plan.active) DailyAlarmScheduler(appContext).schedule(job, plan.hour, plan.minute, replace = true)
                } catch (e: Exception) {
                    Log.e(TAG, "Re-arming $job failed", e)
                }
                pending.finish()
            }
        }
    }

    private companion object { const val TAG = "DailyJobReceiver" }
}
