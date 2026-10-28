package com.dailydivine.app.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReligionsContentTest {

    @Test fun idsAreUnique() {
        assertEquals(Religions.ALL.size, Religions.ALL.map { it.id }.toSet().size)
    }

    @Test fun everyDeclaredContentFileExists() {
        // Gradle runs unit tests with the module directory as working directory.
        for (r in Religions.ALL.filter { it.hasContent }) {
            val file = File("src/main/assets/content/${r.contentAssetEn}")
            assertTrue("${r.name}: missing ${file.path}", file.exists())
        }
    }

    @Test fun hasContentMatchesTheAssetDeclaration() {
        for (r in Religions.ALL) assertEquals(r.contentAssetEn != null, r.hasContent)
    }

    @Test fun atLeastOneTraditionHasContentSoOnboardingCanFinish() {
        assertTrue(Religions.ALL.any { it.hasContent })
    }
}
