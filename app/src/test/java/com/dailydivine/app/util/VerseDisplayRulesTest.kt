package com.dailydivine.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerseDisplayRulesTest {

    @Test fun doubleHyphenBecomesAnEmDash() {
        assertEquals("memory\u2014all betrayed\u2014", VerseDisplayRules.normalize("memory--all betrayed--"))
    }

    @Test fun singleHyphenAndPlainTextAreUntouched() {
        assertEquals("well-known path", VerseDisplayRules.normalize("well-known path"))
        assertEquals("", VerseDisplayRules.normalize(""))
    }

    @Test fun longerVersesGetSmallerType() {
        assertEquals(24, VerseDisplayRules.fontSizeSp(60))
        assertEquals(24, VerseDisplayRules.fontSizeSp(140))
        assertEquals(21, VerseDisplayRules.fontSizeSp(141))
        assertEquals(21, VerseDisplayRules.fontSizeSp(260))
        assertEquals(18, VerseDisplayRules.fontSizeSp(261))
        assertEquals(18, VerseDisplayRules.fontSizeSp(900))
    }

    @Test fun sizeNeverIncreasesWithLength() {
        var previous = Int.MAX_VALUE
        for (len in 0..1000 step 10) {
            val size = VerseDisplayRules.fontSizeSp(len)
            assertTrue(size <= previous)
            previous = size
        }
    }
}
