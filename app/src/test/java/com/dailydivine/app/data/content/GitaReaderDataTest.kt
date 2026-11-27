package com.dailydivine.app.data.content

import com.google.gson.Gson
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the Gita reader file (assets/gita/gita_reader.json) against data mistakes that only show on a device. */
class GitaReaderDataTest {
    private val book: GitaBook = Gson().fromJson(File("src/main/assets/gita/gita_reader.json").readText(Charsets.UTF_8), GitaBook::class.java)
    private val expectedVerses = listOf(47, 72, 43, 42, 29, 47, 30, 28, 34, 42, 55, 20, 34, 27, 20, 24, 28, 78)

    @Test fun hasEighteenChaptersInOrder() {
        assertEquals((1..18).toList(), book.chapters.map { it.number })
        assertTrue(book.chapters.all { it.title.isNotBlank() })
    }

    @Test fun everyChapterCoversItsVersesExactlyOnceInStandardNumbering() {
        for (chapter in book.chapters) {
            val covered = chapter.passages.filterNot { it.id.endsWith(".0") }.flatMap { p ->
                val n = Regex("\\d+").findAll(p.label).map { it.value.toInt() }.toList()
                (n.first()..n.last()).toList()
            }
            assertEquals("chapter ${chapter.number}", (1..expectedVerses[chapter.number - 1]).toList(), covered.sorted())
        }
    }

    @Test fun passageIdsAreUniqueAndEveryPassageHasSanskritAndEnglish() {
        val ids = book.chapters.flatMap { c -> c.passages.map { it.id } }
        assertEquals("duplicate passage ids", ids.size, ids.toSet().size)
        for (p in book.chapters.flatMap { it.passages }) {
            assertTrue("no Sanskrit for ${p.id}", p.sanskrit.any { it in '\u0900'..'\u097F' })
            assertTrue("no English for ${p.id}", p.english.isNotBlank())
            assertFalse("a double space in ${p.id}", p.english.contains("  "))
        }
    }

    @Test fun ownMeaningsComeInPairsAndAreNeverOnTheExcludedVerse() {
        val own = book.chapters.flatMap { it.passages }.filter { it.meaningEn != null || it.meaningHi != null }
        assertEquals(49, own.size)
        assertTrue(own.all { !it.meaningEn.isNullOrBlank() && !it.meaningHi.isNullOrBlank() })
        assertFalse("3.35 is deliberately without a plain-words meaning", own.any { it.id == "3.35" })
    }
}
