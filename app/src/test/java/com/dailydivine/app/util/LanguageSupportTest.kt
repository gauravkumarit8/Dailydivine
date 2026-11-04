package com.dailydivine.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageSupportTest {
    @Test fun englishIsAvailable() {
        assertTrue(LanguageSupport.isAvailable("en"))
    }

    @Test fun otherLanguagesAreNotYet() {
        for (code in listOf("hi", "sa", "ta", "ar", "es", "")) assertFalse(code, LanguageSupport.isAvailable(code))
    }

    @Test fun everyTraditionOffersAnAvailableLanguage() {
        // Otherwise a user of that tradition could not complete the Language step.
        for (r in Religions.ALL) {
            assertTrue("${r.name} has no available language", r.languages.any { LanguageSupport.isAvailable(it.second) })
        }
    }
}
