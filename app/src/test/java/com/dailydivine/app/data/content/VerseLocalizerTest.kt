package com.dailydivine.app.data.content

import com.dailydivine.app.data.local.entity.Verse
import com.dailydivine.app.domain.DailyVerseSelector
import com.dailydivine.app.util.LanguageSupport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class VerseLocalizerTest {
    private val verse = Verse(
        id = 1147, religionId = 1, categoryId = 113, dayNumber = 47,
        originalText = "sanskrit", translatedText = "English text", sourceReference = "Bhagavad Gita 2.66",
        languageCode = "en"
    )

    @Test fun `a translation replaces the text and the language`() {
        val out = VerseLocalizer.apply(verse, "hi", OverlayVerse(1147, 47, "हिन्दी पाठ"))
        assertEquals("हिन्दी पाठ", out.translatedText)
        assertEquals("hi", out.languageCode)
        // Everything else, in particular the id the streaks, favourites and journal point at, is unchanged.
        assertEquals(verse.id, out.id)
        assertEquals("sanskrit", out.originalText)
        assertEquals("Bhagavad Gita 2.66", out.sourceReference)
    }

    @Test fun `a verse without a translation stays exactly as it was`() {
        assertSame(verse, VerseLocalizer.apply(verse, "hi", null))
    }

    @Test fun `a blank translation never blanks the screen`() {
        assertSame(verse, VerseLocalizer.apply(verse, "hi", OverlayVerse(1147, 47, "  ")))
    }

    @Test fun `consecutive day numbers form a range`() {
        val ov = listOf(OverlayVerse(3, 49, "c"), OverlayVerse(1, 47, "a"), OverlayVerse(2, 48, "b"))
        assertEquals(47..49, VerseLocalizer.rangeOf(ov))
    }

    @Test fun `a gap or an empty overlay gives no range`() {
        assertNull(VerseLocalizer.rangeOf(emptyList()))
        assertNull(VerseLocalizer.rangeOf(listOf(OverlayVerse(1, 47, "a"), OverlayVerse(2, 49, "b"))))
    }

    @Test fun `the daily block cycles through its own verses and never leaves them`() {
        val seen = (0L..120L).map { DailyVerseSelector.dayNumberInBlock(it, 47, 49) }
        assertEquals(47, seen.first())
        assertEquals(95, seen[48])
        assertEquals(47, seen[49]) // wraps after 49 days
        assertTrue(seen.all { it in 47..95 })
        // a clock set before the install date must still land inside the block
        assertTrue(DailyVerseSelector.dayNumberInBlock(-5L, 47, 49) in 47..95)
    }

    @Test fun `Hindi is offered for Hinduism only`() {
        assertTrue(LanguageSupport.isAvailable(1, "hi"))
        assertFalse(LanguageSupport.isAvailable(2, "hi"))
        assertFalse(LanguageSupport.isAvailable(null, "hi"))
        assertTrue(LanguageSupport.isAvailable(2, "en"))
        assertEquals(listOf("en", "hi"), LanguageSupport.optionsFor(1).map { it.second })
        assertEquals(listOf("en"), LanguageSupport.optionsFor(2).map { it.second })
    }
}
