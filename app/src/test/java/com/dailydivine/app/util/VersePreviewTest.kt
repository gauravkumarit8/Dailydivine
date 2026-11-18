package com.dailydivine.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersePreviewTest {
    @Test fun `an English verse stops at its first sentence`() {
        assertEquals("Be still.", VersePreview.firstSentence("Be still. And know that I am God."))
    }

    @Test fun `poem line breaks are flattened`() {
        assertEquals("Find full reward Of doing right in right!", VersePreview.firstSentence("Find full reward\nOf doing right in right! Let right deeds be"))
    }

    @Test fun `a Hindi verse stops at the danda and keeps its last word whole`() {
        val hindi = "\u092E\u0928\u0941\u0937\u094D\u092F \u0915\u094B \u091A\u093E\u0939\u093F\u090F \u0915\u093F \u0935\u0939 \u0905\u092A\u0928\u0947 \u0926\u094D\u0935\u093E\u0930\u093E \u0905\u092A\u0928\u093E \u0909\u0926\u094D\u0927\u093E\u0930 \u0915\u0930\u0947\u0964 \u0914\u0930 \u092C\u093E\u0915\u0940"
        val out = VersePreview.firstSentence(hindi)
        assertTrue(out.endsWith("\u0964"))
        assertFalse(out.contains("\u092C\u093E\u0915\u0940"))
    }

    @Test fun `a long sentence is cut at a word boundary with an ellipsis`() {
        val long = "word ".repeat(80).trim()
        val out = VersePreview.firstSentence(long, maxChars = 50)
        assertTrue(out.endsWith("\u2026"))
        assertTrue(out.length <= 50)
        assertFalse(out.dropLast(1).endsWith("wor")) // never splits a word
    }
}
