package com.dailydivine.app.data.content

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards every bundled content file against data mistakes that only show up on a device
 * (duplicate ids, a gap in dayNumber, a stale verseCount, an empty text). Unit tests run in the
 * module directory.
 */
class ContentIntegrityTest {

    private val dir = File("src/main/assets/content")

    private fun files(): List<File> = dir.listFiles { f -> f.extension == "json" }?.sortedBy { it.name }.orEmpty()

    private fun parse(f: File): JsonObject = JsonParser.parseString(f.readText(Charsets.UTF_8)).asJsonObject

    @Test fun contentFilesExist() {
        assertTrue("no content files found in $dir", files().isNotEmpty())
    }

    @Test fun idsAreUniqueAndDayNumbersAreSequential() {
        for (f in files()) {
            val verses = parse(f).getAsJsonArray("verses")
            if (verses.size() == 0) continue
            val ids = verses.map { it.asJsonObject.get("id").asInt }
            assertEquals("${f.name}: duplicate ids", ids.size, ids.toSet().size)
            val days = verses.map { it.asJsonObject.get("dayNumber").asInt }
            assertEquals("${f.name}: dayNumbers must be exactly 1..N", (1..verses.size()).toList(), days.sorted())
        }
    }

    @Test fun textAndSourceAreNeverEmpty() {
        for (f in files()) {
            parse(f).getAsJsonArray("verses").forEach {
                val v = it.asJsonObject
                assertFalse("${f.name} id ${v.get("id")}: empty text", v.get("translatedText").asString.isBlank())
                assertFalse("${f.name} id ${v.get("id")}: empty source", v.get("sourceReference").asString.isBlank())
            }
        }
    }

    @Test fun categoriesAreKnownAndCountsMatch() {
        for (f in files()) {
            val doc = parse(f)
            val verses = doc.getAsJsonArray("verses")
            val cats = doc.getAsJsonArray("categories")
            val declared = cats.associate { it.asJsonObject.get("id").asInt to it.asJsonObject.get("verseCount").asInt }
            val actual = verses.groupingBy { it.asJsonObject.get("categoryId").asInt }.eachCount()
            for (k in actual.keys) assertTrue("${f.name}: verse uses unknown category $k", k in declared)
            for ((k, n) in declared) assertEquals("${f.name}: verseCount of category $k", n, actual[k] ?: 0)
        }
    }

    /** The first 79 Christianity passages shipped in version 1 and must never change (users may have them saved). */
    @Test fun christianityFirst79VersesAreFrozen() {
        val verses = parse(File(dir, "christianity_en.json")).getAsJsonArray("verses")
        assertTrue(verses.size() >= 79)
        val blob = (0 until 79).joinToString("\n") {
            val v = verses[it].asJsonObject
            "${v.get("id").asInt}|${v.get("dayNumber").asInt}|${v.get("sourceReference").asString}|${v.get("translatedText").asString}"
        }
        val hash = MessageDigest.getInstance("SHA-256").digest(blob.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        assertEquals("4ee2f245c31919e5da8526281a896e5b0ea1162d03e541beb4da374b2b79eb95", hash)
    }
}
