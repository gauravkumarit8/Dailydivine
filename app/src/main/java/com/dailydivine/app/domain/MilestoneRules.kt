package com.dailydivine.app.domain

/**
 * F009-R10/R11: when to celebrate a streak milestone.
 *
 * `lastCelebrated` is the highest milestone already celebrated in the CURRENT streak.
 * It used to be kept forever, so after a streak broke the user never saw the 7/14/30-day
 * celebrations again until they beat their old record. A streak shorter than the stored
 * value means the streak was broken and restarted, so the memory is reset.
 *
 * Pure (no Android) so it is unit-tested.
 */
object MilestoneRules {

    data class Decision(
        /** The milestone to celebrate now, or null. */
        val celebrateDays: Int?,
        /** The value to store as "last celebrated" (equal to the old one if unchanged). */
        val newLastCelebrated: Int
    )

    fun decide(currentStreak: Int, milestoneDaysToday: Int?, lastCelebrated: Int): Decision {
        val last = if (currentStreak < lastCelebrated) 0 else lastCelebrated
        val celebrate = milestoneDaysToday?.takeIf { it == currentStreak && it > last }
        return Decision(celebrate, celebrate ?: last)
    }
}
