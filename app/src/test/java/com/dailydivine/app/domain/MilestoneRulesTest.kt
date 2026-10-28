package com.dailydivine.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MilestoneRulesTest {

    @Test fun firstTimeMilestoneIsCelebratedAndRemembered() {
        val d = MilestoneRules.decide(currentStreak = 7, milestoneDaysToday = 7, lastCelebrated = 0)
        assertEquals(7, d.celebrateDays)
        assertEquals(7, d.newLastCelebrated)
    }

    @Test fun reopeningTheSameDayDoesNotRepeat() {
        val d = MilestoneRules.decide(7, 7, lastCelebrated = 7)
        assertNull(d.celebrateDays)
        assertEquals(7, d.newLastCelebrated)
    }

    @Test fun brokenStreakForgetsOldMilestonesSoSevenCelebratesAgain() {
        // Had celebrated 30 days, broke, restarted: streak is 1 -> memory resets.
        val afterBreak = MilestoneRules.decide(1, null, lastCelebrated = 30)
        assertNull(afterBreak.celebrateDays)
        assertEquals(0, afterBreak.newLastCelebrated)
        // ...and day 7 of the new streak is celebrated (it was silent before this fix).
        val day7 = MilestoneRules.decide(7, 7, lastCelebrated = afterBreak.newLastCelebrated)
        assertEquals(7, day7.celebrateDays)
    }

    @Test fun ordinaryDaysDoNothing() {
        val d = MilestoneRules.decide(8, null, lastCelebrated = 7)
        assertNull(d.celebrateDays)
        assertEquals(7, d.newLastCelebrated)
    }

    @Test fun milestoneBelowOrEqualToLastIsNotRepeatedInTheSameStreak() {
        assertNull(MilestoneRules.decide(14, 14, lastCelebrated = 14).celebrateDays)
        assertEquals(14, MilestoneRules.decide(14, 14, lastCelebrated = 7).celebrateDays)
    }

    @Test fun milestoneMustMatchTheCurrentStreakExactly() {
        assertNull(MilestoneRules.decide(9, 7, lastCelebrated = 0).celebrateDays)
    }
}
