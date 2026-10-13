package com.dailydivine.app.jobs

import com.dailydivine.app.data.local.datastore.UserPrefsState
import com.dailydivine.app.wallpaper.WallpaperTarget

/** The three once-a-day background jobs. */
enum class DailyJob(val requestCode: Int) {
    WALLPAPER(7101),
    DAILY_VERSE(7102),
    STREAK_REMINDER(7103);

    companion object {
        /** Null for anything unknown: a stray or stale broadcast must do nothing. */
        fun fromName(name: String?): DailyJob? = values().firstOrNull { it.name == name }
    }
}

/** Whether a job should currently be scheduled, and at what time of day. */
data class JobPlan(val active: Boolean, val hour: Int, val minute: Int)

/** Pure: turns the saved settings into the plan for [job] (unit-tested). */
fun planFor(job: DailyJob, p: UserPrefsState): JobPlan = when (job) {
    DailyJob.WALLPAPER -> JobPlan(
        active = p.onboardingCompleted && WallpaperTarget.fromName(p.wallpaperTarget) != WallpaperTarget.OFF,
        hour = p.wallpaperHour, minute = p.wallpaperMinute
    )
    DailyJob.DAILY_VERSE -> JobPlan(
        active = p.onboardingCompleted && p.dailyVerseEnabled,
        hour = p.dailyVerseHour, minute = p.dailyVerseMinute
    )
    DailyJob.STREAK_REMINDER -> JobPlan( // F009-R06: fixed 8 PM
        active = p.onboardingCompleted && p.streakReminderEnabled, hour = 20, minute = 0
    )
}

fun plansFor(p: UserPrefsState): Map<DailyJob, JobPlan> = DailyJob.values().associateWith { planFor(it, p) }
