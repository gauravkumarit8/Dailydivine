package com.dailydivine.app.data.repository

import com.dailydivine.app.data.local.dao.StreakDao
import com.dailydivine.app.data.local.dao.VerseDao
import com.dailydivine.app.data.local.entity.StreakEntry
import com.dailydivine.app.data.local.entity.Verse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * F011 behaviour: reflection + mood are stored on the day's streak entry,
 * blank text is stored as null, a save never disturbs the rest of the entry,
 * and saving for a day with no entry changes nothing.
 */
class JournalRepositoryTest {

    private class FakeStreakDao : StreakDao {
        val rows = MutableStateFlow<List<StreakEntry>>(emptyList())
        private var nextId = 1

        override suspend fun insertStreakEntry(entry: StreakEntry) {
            rows.value = rows.value.filter { it.date != entry.date } + entry.copy(id = nextId++)
        }
        override suspend fun updateJournal(date: String, journalText: String?, moodEmoji: String?) {
            rows.value = rows.value.map {
                if (it.date == date) it.copy(journalText = journalText, moodEmoji = moodEmoji) else it
            }
        }
        override suspend fun getEntryForDate(date: String): StreakEntry? = rows.value.firstOrNull { it.date == date }
        override suspend fun getAllDates(): List<String> = rows.value.map { it.date }.sorted()
        override fun getAllEntries(): Flow<List<StreakEntry>> =
            MutableStateFlow(rows.value.sortedByDescending { it.date })
        override fun getRecentEntries(days: Int): Flow<List<StreakEntry>> = getAllEntries()
        override suspend fun getStreakCount(startDate: String): Int = rows.value.count { it.date >= startDate }
    }

    private val verse = Verse(
        id = 7, religionId = 2, categoryId = 201, dayNumber = 1, originalText = "",
        translatedText = "The LORD is my shepherd.", sourceReference = "Psalm 23:1 (KJV)", languageCode = "en"
    )
    private val today = LocalDate.of(2026, 10, 3)

    private fun repo(dao: FakeStreakDao): JournalRepository {
        val verseDao = mockk<VerseDao>()
        coEvery { verseDao.getVerseById(7) } returns verse
        coEvery { verseDao.getVerseById(99) } returns null
        return JournalRepository(dao, verseDao)
    }

    private suspend fun open(dao: FakeStreakDao, date: LocalDate, verseId: Int = 7) =
        dao.insertStreakEntry(StreakEntry(date = date.toString(), verseId = verseId, openedAt = 1L))

    @Test
    fun `saved reflection and mood are read back the same day`() = runBlocking {
        val dao = FakeStreakDao(); open(dao, today)
        repo(dao).save(today, "Peace in the morning", "\uD83D\uDE0C")
        val entry = repo(dao).getEntry(today)!!
        assertEquals("Peace in the morning", entry.journalText)
        assertEquals("\uD83D\uDE0C", entry.moodEmoji)
    }

    @Test
    fun `blank text is stored as null so history shows no reflection`() = runBlocking {
        val dao = FakeStreakDao(); open(dao, today)
        repo(dao).save(today, "   ", null)
        assertNull(repo(dao).getEntry(today)!!.journalText)
    }

    @Test
    fun `saving keeps the verse link and does not create or reset the entry`() = runBlocking {
        val dao = FakeStreakDao(); open(dao, today)
        val before = repo(dao).getEntry(today)!!
        repo(dao).save(today, "note", null)
        val after = repo(dao).getEntry(today)!!
        assertEquals(before.id, after.id)
        assertEquals(before.verseId, after.verseId)
        assertEquals(before.openedAt, after.openedAt)
        assertEquals(1, dao.rows.value.size)
    }

    @Test
    fun `saving for a day with no entry is a no-op`() = runBlocking {
        val dao = FakeStreakDao()
        repo(dao).save(today, "orphan", "\uD83D\uDE0A")
        assertEquals(0, dao.rows.value.size)
    }

    @Test
    fun `a save lands on the day it was written for, not on today`() = runBlocking {
        val dao = FakeStreakDao()
        open(dao, today.minusDays(1)); open(dao, today)
        repo(dao).save(today.minusDays(1), "written before midnight", null)
        assertEquals("written before midnight", repo(dao).getEntry(today.minusDays(1))!!.journalText)
        assertNull(repo(dao).getEntry(today)!!.journalText)
    }

    @Test
    fun `history is newest first and joins each day's verse`() = runBlocking {
        val dao = FakeStreakDao()
        open(dao, today.minusDays(1)); open(dao, today, verseId = 99)
        val items = repo(dao).history().first()
        assertEquals(listOf(today.toString(), today.minusDays(1).toString()), items.map { it.entry.date })
        assertNull(items[0].verse)                       // verse 99 no longer exists
        assertEquals("Psalm 23:1 (KJV)", items[1].verse!!.sourceReference)
    }
}
