package com.dailydivine.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class VerseAttributionTest {
    @Test fun `gita verses name who wrote the meaning`() {
        assertEquals("Bhagavad Gita 9.29 \u00B7 Hindi meaning by DailyDivine", VerseAttribution.line("Bhagavad Gita 9.29", "hi"))
        assertEquals("Bhagavad Gita 9.29 \u00B7 English meaning by DailyDivine", VerseAttribution.line("Bhagavad Gita 9.29", "en"))
    }

    @Test fun `other sources already name their translator and are left alone`() {
        val arnold = "The Song Celestial, Ch. II (Arnold, 1885)"
        assertEquals(arnold, VerseAttribution.line(arnold, "en"))
        assertEquals("Psalm 23:1-4 (KJV)", VerseAttribution.line("Psalm 23:1-4 (KJV)", "hi"))
    }
}
