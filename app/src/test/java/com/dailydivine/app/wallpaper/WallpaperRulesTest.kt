package com.dailydivine.app.wallpaper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperRulesTest {

    // ---- when to update
    @Test fun `off never updates`() =
        assertFalse(WallpaperRules.needsUpdate(WallpaperTarget.OFF, lastUpdatedEpochDay = -1, todayEpochDay = 20000))

    @Test fun `never applied before updates`() =
        assertTrue(WallpaperRules.needsUpdate(WallpaperTarget.BOTH, -1, 20000))

    @Test fun `already applied today does not repeat`() =
        assertFalse(WallpaperRules.needsUpdate(WallpaperTarget.HOME, 20000, 20000))

    @Test fun `applied yesterday updates today`() =
        assertTrue(WallpaperRules.needsUpdate(WallpaperTarget.LOCK, 19999, 20000))

    // ---- targets: never apply by accident
    @Test fun `unknown or missing target falls back to off`() {
        assertEquals(WallpaperTarget.OFF, WallpaperTarget.fromName(null))
        assertEquals(WallpaperTarget.OFF, WallpaperTarget.fromName("garbage"))
        assertEquals(WallpaperTarget.OFF, WallpaperTarget.fromName(""))
    }

    @Test fun `targets map to the right screens`() {
        assertTrue(WallpaperTarget.HOME.home && !WallpaperTarget.HOME.lock)
        assertTrue(!WallpaperTarget.LOCK.home && WallpaperTarget.LOCK.lock)
        assertTrue(WallpaperTarget.BOTH.home && WallpaperTarget.BOTH.lock)
        assertTrue(!WallpaperTarget.OFF.home && !WallpaperTarget.OFF.lock)
    }

    @Test fun `every target survives a name round trip`() =
        WallpaperTarget.values().forEach { assertEquals(it, WallpaperTarget.fromName(it.name)) }

    @Test fun `text position falls back to middle and stays in the safe band`() {
        assertEquals(TextPosition.MIDDLE, TextPosition.fromName(null))
        assertEquals(TextPosition.MIDDLE, TextPosition.fromName("nope"))
        TextPosition.values().forEach { assertTrue(it.centerFraction in 0.25f..0.75f) }
    }

    // ---- importing a user's photo
    @Test fun `normal phone photo is not downsampled`() =
        assertEquals(1, WallpaperRules.sampleSize(4000, 3000))            // exactly 12 MP

    @Test fun `small images are never downsampled`() =
        assertEquals(1, WallpaperRules.sampleSize(1000, 800))

    @Test fun `48 MP photo is halved`() =
        assertEquals(2, WallpaperRules.sampleSize(8000, 6000))            // 4000x3000 = 12 MP

    @Test fun `108 MP photo is quartered so it cannot exhaust memory`() =
        assertEquals(4, WallpaperRules.sampleSize(12000, 9000))           // 3000x2250

    @Test fun `long edge stays at or above the target when downsampling`() {
        val s = WallpaperRules.sampleSize(6500, 4000)                     // 6500/2 = 3250 >= 3200
        assertEquals(2, s)
        assertTrue(6500 / s >= 3200)
    }

    @Test fun `decoded size never exceeds the pixel budget`() {
        listOf(2000 to 1500, 4000 to 3000, 8000 to 6000, 12000 to 9000, 16000 to 12000, 30000 to 200).forEach { (w, h) ->
            val s = WallpaperRules.sampleSize(w, h)
            assertTrue("$w x $h", (w / s).toLong() * (h / s) <= 12_000_000L || s >= 64)
        }
    }

    @Test fun `sample size is always a power of two`() {
        listOf(500 to 500, 5000 to 4000, 9000 to 7000, 20000 to 15000).forEach { (w, h) ->
            val s = WallpaperRules.sampleSize(w, h)
            assertEquals("$w x $h", 0, s and (s - 1))
        }
    }
}
