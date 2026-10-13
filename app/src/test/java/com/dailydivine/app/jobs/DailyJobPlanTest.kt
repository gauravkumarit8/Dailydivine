package com.dailydivine.app.jobs

import com.dailydivine.app.data.local.datastore.UserPrefsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyJobPlanTest {

    private val onboarded = UserPrefsState(onboardingCompleted = true)

    @Test fun `request codes are distinct so the jobs never overwrite each other's alarm`() {
        val codes = DailyJob.values().map { it.requestCode }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test fun `unknown or missing job names do nothing`() {
        assertNull(DailyJob.fromName(null))
        assertNull(DailyJob.fromName("garbage"))
        assertEquals(DailyJob.WALLPAPER, DailyJob.fromName("WALLPAPER"))
    }

    @Test fun `nothing is scheduled before onboarding is complete`() {
        val p = UserPrefsState(onboardingCompleted = false, wallpaperTarget = "BOTH")
        plansFor(p).values.forEach { assertFalse(it.active) }
    }

    @Test fun `wallpaper is off by default and active once a target is chosen`() {
        assertFalse(planFor(DailyJob.WALLPAPER, onboarded).active)
        val on = planFor(DailyJob.WALLPAPER, onboarded.copy(wallpaperTarget = "LOCK", wallpaperHour = 6, wallpaperMinute = 30))
        assertTrue(on.active)
        assertEquals(6, on.hour); assertEquals(30, on.minute)
    }

    @Test fun `an unrecognised wallpaper target never activates the job`() =
        assertFalse(planFor(DailyJob.WALLPAPER, onboarded.copy(wallpaperTarget = "nonsense")).active)

    @Test fun `daily verse follows its switch and its time`() {
        val on = planFor(DailyJob.DAILY_VERSE, onboarded.copy(dailyVerseHour = 8, dailyVerseMinute = 15))
        assertTrue(on.active); assertEquals(8, on.hour); assertEquals(15, on.minute)
        assertFalse(planFor(DailyJob.DAILY_VERSE, onboarded.copy(dailyVerseEnabled = false)).active)
    }

    @Test fun `streak reminder is always 8 PM and follows its switch`() {
        val on = planFor(DailyJob.STREAK_REMINDER, onboarded)
        assertTrue(on.active); assertEquals(20, on.hour); assertEquals(0, on.minute)
        assertFalse(planFor(DailyJob.STREAK_REMINDER, onboarded.copy(streakReminderEnabled = false)).active)
    }

    @Test fun `plansFor covers every job`() =
        assertEquals(DailyJob.values().toSet(), plansFor(onboarded).keys)
}
