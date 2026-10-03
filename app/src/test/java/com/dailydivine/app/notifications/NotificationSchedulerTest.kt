package com.dailydivine.app.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * F009 AC1 depends entirely on getting the delay to the next time-of-day
 * right, including the edge cases (exactly on the minute, already past today).
 */
class NotificationSchedulerTest {

    private fun at(h: Int, m: Int, s: Int = 0) = LocalDateTime.of(2026, 10, 3, h, m, s)

    @Test
    fun `target later today waits until today`() {
        assertEquals(TimeUnit.HOURS.toMillis(1), NotificationScheduler.millisUntilNext(7, 0, at(6, 0)))
    }

    @Test
    fun `target already past today waits until tomorrow`() {
        assertEquals(TimeUnit.HOURS.toMillis(23), NotificationScheduler.millisUntilNext(20, 0, at(21, 0)))
    }

    @Test
    fun `exactly at target time schedules tomorrow, not now`() {
        // Otherwise a worker that renews itself right on the minute would re-fire immediately.
        assertEquals(TimeUnit.HOURS.toMillis(24), NotificationScheduler.millisUntilNext(7, 0, at(7, 0)))
    }

    @Test
    fun `seconds before target are respected`() {
        assertEquals(TimeUnit.SECONDS.toMillis(30), NotificationScheduler.millisUntilNext(20, 0, at(19, 59, 30)))
    }

    @Test
    fun `crosses midnight correctly`() {
        // 23:30 -> 07:00 next day = 7.5 hours
        assertEquals(TimeUnit.MINUTES.toMillis(450), NotificationScheduler.millisUntilNext(7, 0, at(23, 30)))
    }
}
