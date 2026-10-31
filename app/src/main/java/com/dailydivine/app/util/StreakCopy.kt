package com.dailydivine.app.util

/** Wording for the streak chip on Home. Pure so it is unit-tested (singular/plural). */
object StreakCopy {
    fun title(days: Int): String = "$days-day streak"

    fun next(milestoneName: String, daysLeft: Int): String {
        val left = daysLeft.coerceAtLeast(0)
        return "Next: $milestoneName in $left ${if (left == 1) "day" else "days"}"
    }
}
