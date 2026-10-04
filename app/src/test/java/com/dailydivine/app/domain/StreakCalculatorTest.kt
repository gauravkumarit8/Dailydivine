package com.dailydivine.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 10, 3)
    private fun daysAgo(vararg n: Int) = n.map { today.minusDays(it.toLong()) }

    @Test
    fun `no history is all zeros`() {
        assertEquals(StreakStats(0, 0, 0), StreakCalculator.compute(emptyList(), today))
    }

    @Test
    fun `first ever open gives a streak of one`() {
        assertEquals(StreakStats(1, 1, 1), StreakCalculator.compute(daysAgo(0), today))
    }

    @Test
    fun `consecutive days count up to today`() {
        assertEquals(StreakStats(3, 3, 3), StreakCalculator.compute(daysAgo(0, 1, 2), today))
    }

    @Test
    fun `a missed day resets current to zero but keeps the earlier run as longest`() {
        // opened 4,3,2 days ago, skipped yesterday, not yet opened today
        assertEquals(StreakStats(0, 3, 3), StreakCalculator.compute(daysAgo(4, 3, 2), today))
    }

    @Test
    fun `a gap splits runs and longest is the best one even if it is old`() {
        // old run of 5, gap, current run of 2
        val dates = daysAgo(0, 1) + daysAgo(10, 11, 12, 13, 14)
        assertEquals(StreakStats(2, 5, 7), StreakCalculator.compute(dates, today))
    }

    @Test
    fun `streak ending yesterday is not current until today is recorded`() {
        assertEquals(0, StreakCalculator.compute(daysAgo(1, 2, 3), today).current)
        assertEquals(4, StreakCalculator.compute(daysAgo(0, 1, 2, 3), today).current)
    }

    @Test
    fun `duplicate dates are counted once`() {
        assertEquals(StreakStats(2, 2, 2), StreakCalculator.compute(daysAgo(0, 0, 1, 1), today))
    }

    @Test
    fun `runs are found across month and year boundaries`() {
        val newYear = LocalDate.of(2027, 1, 2)
        val dates = listOf(LocalDate.of(2026, 12, 30), LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1), newYear)
        assertEquals(StreakStats(4, 4, 4), StreakCalculator.compute(dates, newYear))
    }
}
