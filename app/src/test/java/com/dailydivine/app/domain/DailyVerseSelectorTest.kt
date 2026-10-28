package com.dailydivine.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyVerseSelectorTest {

    @Test fun installDayIsVerseOneAndItWrapsAround() {
        assertEquals(1, DailyVerseSelector.dayNumber(0, 34))
        assertEquals(2, DailyVerseSelector.dayNumber(1, 34))
        assertEquals(34, DailyVerseSelector.dayNumber(33, 34))
        assertEquals(1, DailyVerseSelector.dayNumber(34, 34))
    }

    @Test fun clockBeforeInstallDateStillGivesAValidVerse() {
        // Plain % gave 0 or negative numbers here (an empty Home screen).
        assertEquals(34, DailyVerseSelector.dayNumber(-1, 34))
        assertEquals(1, DailyVerseSelector.dayNumber(-34, 34))
        for (days in -200L..200L) {
            val n = DailyVerseSelector.dayNumber(days, 46)
            assertTrue("dayNumber $n out of range for $days", n in 1..46)
        }
    }

    @Test fun singleVerseAlwaysReturnsOne() {
        assertEquals(1, DailyVerseSelector.dayNumber(12345, 1))
        assertEquals(1, DailyVerseSelector.dayNumber(-7, 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroVersesIsRejected() {
        DailyVerseSelector.dayNumber(1, 0)
    }
}
