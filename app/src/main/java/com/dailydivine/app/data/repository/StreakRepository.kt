package com.dailydivine.app.data.repository

import com.dailydivine.app.data.local.dao.StreakDao
import com.dailydivine.app.data.local.entity.StreakEntry
import com.dailydivine.app.domain.model.StreakInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class StreakRepository @Inject constructor(private val streakDao: StreakDao) {

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun recordOpenedToday(verseId: Int) {
        val today = LocalDate.now().format(isoFormatter)
        if (streakDao.getEntryForDate(today) == null) {
            streakDao.insertStreakEntry(
                StreakEntry(
                    date = today, verseId = verseId, wasRead = true,
                    wasListened = false, journalText = null, moodEmoji = null,
                    openedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /** F009-R08: true if the app was already opened today (a streak entry exists). */
    suspend fun hasOpenedToday(): Boolean =
        streakDao.getEntryForDate(LocalDate.now().format(isoFormatter)) != null

    /** F009-R07/R09: the streak as of YESTERDAY. calculateStreak() starts at
     *  today and so returns 0 when the user hasn't opened the app yet today,
     *  which is exactly the situation the 8 PM reminder runs in. */
    suspend fun streakEndingYesterday(): Int {
        var count = 0
        var date = LocalDate.now().minusDays(1)
        while (streakDao.getEntryForDate(date.format(isoFormatter)) != null) {
            count++
            date = date.minusDays(1)
        }
        return count
    }

    /** F003: consecutive-day count walking backwards from today. */
    suspend fun calculateStreak(): StreakInfo {
        var current = 0
        var date = LocalDate.now()
        while (true) {
            val entry = streakDao.getEntryForDate(date.format(isoFormatter))
            if (entry == null) break
            current++
            date = date.minusDays(1)
        }
        val currentMilestone = StreakInfo.MILESTONES.lastOrNull { it.days <= current }
        val nextMilestone = StreakInfo.MILESTONES.firstOrNull { it.days > current }
        val progress = if (nextMilestone != null) {
            val prevDays = currentMilestone?.days ?: 0
            (current - prevDays).toFloat() / (nextMilestone.days - prevDays).toFloat()
        } else 1f

        return StreakInfo(
            currentStreak = current,
            longestStreak = current, // TODO: track separately once historical max is needed
            totalDaysActive = current,
            currentMilestone = currentMilestone,
            nextMilestone = nextMilestone,
            progressToNext = progress.coerceIn(0f, 1f),
            hasFreezeAvailable = false // TODO: Sprint 3 premium streak-freeze logic (F003-R07/09)
        )
    }
}
