package com.dailydivine.app.domain

import java.time.LocalDate

data class StreakStats(val current: Int, val longest: Int, val total: Int)

/**
 * Pure streak maths (F003-R01/R02), kept free of Android and Room so it can be
 * unit-tested exhaustively. Before this, StreakRepository reported
 * longestStreak and totalDaysActive as simply the current streak, so a user
 * who broke a 30-day streak would also lose the "Monthly Devotee" badge.
 *
 * - current: consecutive days ending TODAY (0 if today has no entry).
 * - longest: longest consecutive run anywhere in history (never below current).
 * - total:   distinct days the app was opened.
 */
object StreakCalculator {
    fun compute(dates: Collection<LocalDate>, today: LocalDate): StreakStats {
        val days = dates.toSet()

        var current = 0
        var d = today
        while (d in days) { current++; d = d.minusDays(1) }

        var longest = 0
        var run = 0
        var previous: LocalDate? = null
        for (day in days.sorted()) {
            run = if (previous != null && previous.plusDays(1) == day) run + 1 else 1
            if (run > longest) longest = run
            previous = day
        }
        return StreakStats(current = current, longest = maxOf(longest, current), total = days.size)
    }
}
