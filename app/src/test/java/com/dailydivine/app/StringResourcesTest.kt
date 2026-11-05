package com.dailydivine.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards res/values/strings.xml, which a typo can break at build time (an unescaped apostrophe is an
 * aapt error) or at run time (a missing key crashes with Resources.NotFoundException). Unit tests run
 * in the module directory.
 */
class StringResourcesTest {

    private val xml = File("src/main/res/values/strings.xml").readText()
    private val names = Regex("""<string name="([^"]+)"""").findAll(xml).map { it.groupValues[1] }.toList()

    @Test fun noDuplicateKeys() {
        assertEquals("duplicate string names", emptyList<String>(), names.groupBy { it }.filter { it.value.size > 1 }.keys.toList())
    }

    @Test fun everyReferencedKeyExists() {
        val used = mutableSetOf<String>()
        File("src/main/java").walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            Regex("""R\.string\.([A-Za-z0-9_]+)""").findAll(file.readText()).forEach { used += it.groupValues[1] }
        }
        val missing = used - names.toSet()
        assertTrue("R.string keys used in code but missing from strings.xml: $missing", missing.isEmpty())
    }

    @Test fun apostrophesAndQuotesAreEscaped() {
        for (m in Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL).findAll(xml)) {
            val value = m.groupValues[2]
            assertTrue("${m.groupValues[1]}: unescaped apostrophe", !Regex("""(?<!\\)'""").containsMatchIn(value))
            assertTrue("${m.groupValues[1]}: unescaped double quote", !Regex("""(?<!\\)"""").containsMatchIn(value))
        }
    }

    @Test fun keysAreValidAndNotReservedWords() {
        // CI failed once because a generated key was "continue": aapt rejects Java keywords (and the
        // Kotlin ones would need backticks in R.string.x).
        val reserved = (
            "abstract assert boolean break byte case catch char class const continue default do double else enum " +
                "extends final finally float for goto if implements import instanceof int interface long native new " +
                "package private protected public return short static strictfp super switch synchronized this throw " +
                "throws transient try void volatile while true false null as fun in is object typealias typeof val var when"
            ).split(" ").toSet()
        for (name in names) {
            assertTrue("$name is not a valid resource name", Regex("[A-Za-z_][A-Za-z0-9_]*").matches(name))
            assertTrue("$name is a reserved word", name !in reserved)
        }
    }

    @Test fun noFormatSpecifiersWithoutProperEscaping() {
        // Plain strings must not contain a bare % (aapt treats it as a format specifier).
        for (m in Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL).findAll(xml)) {
            assertTrue("${m.groupValues[1]} contains %", !m.groupValues[2].contains('%'))
        }
    }
}
