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

    @Test fun straightQuotesInsideAVerseBecomeCurlySingleQuotes() {
        // The Home screen wraps the verse in curly double quotes, so inner quotes are single.
        assertEquals(
            "And taking new ones, sayeth,\n\u2018These will I wear to-day!\u2019",
            VerseDisplayRules.normalize("And taking new ones, sayeth,\n\"These will I wear to-day!\"")
        )
    }

    @Test fun apostrophesBecomeTheRightSingleQuote() {
        assertEquals("O\u2019er the hills, the soul\u2019s", VerseDisplayRules.normalize("O'er the hills, the soul's"))
    }

    @Test fun quoteAfterADashOrBracketOpens() {
        assertEquals("(\u2018Hail\u2019)", VerseDisplayRules.normalize("(\"Hail\")"))
        assertEquals("said\u2014\u2018go\u2019", VerseDisplayRules.normalize("said--\"go\""))
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
