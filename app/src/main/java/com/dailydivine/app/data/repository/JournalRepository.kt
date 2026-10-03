package com.dailydivine.app.data.repository

import com.dailydivine.app.data.local.dao.StreakDao
import com.dailydivine.app.data.local.dao.VerseDao
import com.dailydivine.app.data.local.entity.StreakEntry
import com.dailydivine.app.domain.model.JournalHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * F011: reflection + mood, stored on the day's streak entry (F011-R06/R08:
 * linked to the verse and the date). No schema change was needed: the streaks
 * table already had journalText and moodEmoji.
 */
class JournalRepository @Inject constructor(
    private val streakDao: StreakDao,
    private val verseDao: VerseDao
) {
    private val iso = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun getEntry(date: LocalDate): StreakEntry? = streakDao.getEntryForDate(date.format(iso))

    /** Saves for an explicit [date] (not "now"): a debounced save that fires
     *  just after midnight must still land on the day the text was written.
     *  Blank text is stored as null. A date with no entry is a no-op, because
     *  an entry only exists once that day's verse was shown. */
    suspend fun save(date: LocalDate, text: String, mood: String?) {
        streakDao.updateJournal(date.format(iso), text.ifBlank { null }, mood)
    }

    /** All past days, newest first, each with that day's verse (F011-R04). */
    fun history(): Flow<List<JournalHistoryItem>> =
        streakDao.getAllEntries().map { entries ->
            entries.map { JournalHistoryItem(it, verseDao.getVerseById(it.verseId)) }
        }
}
