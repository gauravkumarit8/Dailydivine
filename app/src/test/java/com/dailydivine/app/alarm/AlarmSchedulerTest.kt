package com.dailydivine.app.alarm

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/**
 * F004-R08 (repeat days) and the next-ring maths. The clock is a parameter, so
 * these are deterministic. Reference dates, October 2026: Fri 2, Sat 3, Sun 4,
 * Mon 5, Mon 12.
 */
class AlarmSchedulerTest {

    private fun at(day: Int, hour: Int, minute: Int, second: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, day, hour, minute, second)
        }.timeInMillis

    private val daily = "[1,2,3,4,5,6,7]"
    private val weekdays = "[1,2,3,4,5]"

    private fun next(days: String, now: Long, hour: Int = 7, minute: Int = 0, lead: Long = 0L) =
        AlarmScheduler.computeNextTriggerTime(hour, minute, days, lead, now)

    @Test fun `daily alarm later today rings today`() =
        assertEquals(at(3, 7, 0), next(daily, at(3, 6, 0)))

    @Test fun `daily alarm already past today rings tomorrow`() =
        assertEquals(at(4, 7, 0), next(daily, at(3, 8, 0)))

    @Test fun `weekday alarm set on a Saturday skips the weekend to Monday`() =
        assertEquals(at(5, 7, 0), next(weekdays, at(3, 8, 0)))

    @Test fun `weekday alarm on Friday before the time rings that Friday`() =
        assertEquals(at(2, 7, 0), next(weekdays, at(2, 6, 0)))

    @Test fun `weekday alarm on Friday after the time rings Monday`() =
        assertEquals(at(5, 7, 0), next(weekdays, at(2, 8, 0)))

    @Test fun `Sunday-only alarm maps ISO day 7 to Sunday`() =
        assertEquals(at(4, 7, 0), next("[7]", at(3, 8, 0)))

    @Test fun `Monday-only alarm exactly at its time waits a full week`() =
        // strictly after now, so a re-arm right on the minute can't ring twice
        assertEquals(at(12, 7, 0), next("[1]", at(5, 7, 0)))

    @Test fun `min lead pushes an almost-due alarm to the next day`() =
        // after-firing re-arm: 07:01 earliest, so today's 07:00 no longer qualifies
        assertEquals(at(4, 7, 0), next(daily, at(3, 6, 59), lead = 2 * 60_000L))

    @Test fun `empty or garbage repeat days mean every day`() {
        assertEquals(at(4, 7, 0), next("[]", at(3, 8, 0)))
        assertEquals(at(4, 7, 0), next("garbage", at(3, 8, 0)))
    }

    @Test fun `parse keeps only valid ISO weekdays`() {
        assertEquals(setOf(1, 2, 3), AlarmScheduler.parseRepeatDays("[1,2,3]"))
        assertEquals(setOf(3), AlarmScheduler.parseRepeatDays("[0,8,3]"))
        assertEquals((1..7).toSet(), AlarmScheduler.parseRepeatDays(""))
    }

    @Test fun `format sorts and brackets the days`() {
        assertEquals("[1,3,5]", AlarmScheduler.formatRepeatDays(setOf(5, 1, 3)))
        assertEquals(setOf(2, 4), AlarmScheduler.parseRepeatDays(AlarmScheduler.formatRepeatDays(setOf(4, 2))))
    }
}
