package com.dailydivine.app.data.repository

import com.dailydivine.app.data.local.dao.BookmarkDao
import com.dailydivine.app.data.local.dao.VerseDao
import com.dailydivine.app.data.local.entity.Verse
import com.dailydivine.app.domain.model.DailyVerse
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class VerseRepository @Inject constructor(
    private val verseDao: VerseDao,
    private val bookmarkDao: BookmarkDao
) {
    /** Implements API Contract 1 (PRD Section 19): deterministic day-number
     *  selection, same verse for all users of a religion on a given date. */
    suspend fun getDailyVerse(religionId: Int, installDate: LocalDate, today: LocalDate): DailyVerse? {
        val totalVerses = verseDao.getVerseCount(religionId).takeIf { it > 0 } ?: return null
        val daysSinceInstall = ChronoUnit.DAYS.between(installDate, today)
        val dayNumber = (daysSinceInstall % totalVerses).toInt() + 1
        val verse = verseDao.getDailyVerse(religionId, dayNumber) ?: return null
        val isBookmarked = bookmarkDao.isBookmarked(verse.id).first()
        return DailyVerse(verse = verse, dayNumber = dayNumber, isBookmarked = isBookmarked, hasJournalEntry = false)
    }

    suspend fun getRandomVerse(religionId: Int): Verse? = verseDao.getRandomVerse(religionId)

    fun searchVerses(religionId: Int, query: String) = verseDao.searchVerses(religionId, query)
}
