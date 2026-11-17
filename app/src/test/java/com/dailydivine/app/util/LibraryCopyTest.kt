package com.dailydivine.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryCopyTest {
    @Test fun verseCountUsesSingularForOne() {
        assertEquals("1 verse", LibraryCopy.verseCount(1))
        assertEquals("0 verses", LibraryCopy.verseCount(0))
        assertEquals("12 verses", LibraryCopy.verseCount(12))
    }

    @Test fun initialIsTheFirstLetterUppercased() {
        assertEquals("D", LibraryCopy.initial("Duty & Action"))
        assertEquals("W", LibraryCopy.initial("  wisdom of the Soul"))
    }

    @Test fun initialSkipsLeadingPunctuationAndFallsBackToABullet() {
        assertEquals("P", LibraryCopy.initial("\u201CPeace\u201D"))
        assertEquals("\u2022", LibraryCopy.initial("   "))
        assertEquals("\u2022", LibraryCopy.initial("&&"))
    }

    @Test fun initialsAreTwoLettersSoSimilarCategoriesDiffer() {
        assertEquals("Du", LibraryCopy.initials("Duty & Action"))
        assertEquals("De", LibraryCopy.initials("Devotion & Virtue"))
        assertEquals("Ps", LibraryCopy.initials("Psalms"))
        assertEquals("Pr", LibraryCopy.initials("Proverbs"))
    }

    @Test fun initialsHandleShortAndEmptyNames() {
        assertEquals("A", LibraryCopy.initials("a"))
        assertEquals("\u2022", LibraryCopy.initials("  & "))
    }
}
