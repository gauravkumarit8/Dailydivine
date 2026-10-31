package com.dailydivine.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCopyTest {
    @Test fun titleIsHyphenated() {
        assertEquals("3-day streak", StreakCopy.title(3))
        assertEquals("1-day streak", StreakCopy.title(1))
    }

    @Test fun nextUsesSingularForOneDayAndPluralOtherwise() {
        assertEquals("Next: Week Warrior in 4 days", StreakCopy.next("Week Warrior", 4))
        assertEquals("Next: Week Warrior in 1 day", StreakCopy.next("Week Warrior", 1))
    }

    @Test fun negativeDaysLeftIsClampedToZero() {
        assertEquals("Next: Week Warrior in 0 days", StreakCopy.next("Week Warrior", -2))
    }
}
