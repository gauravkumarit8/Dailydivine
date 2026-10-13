package com.dailydivine.app.wallpaper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class WallpaperLastCheckTest {
    private fun ms(day: Int, h: Int, m: Int) =
        ZonedDateTime.of(2026, 10, day, h, m, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
    private val now = ms(13, 8, 0)

    @Test fun `no attempt yet gives nothing to show`() =
        assertNull(describeLastCheck(0L, "", "", now, ZoneOffset.UTC))

    @Test fun `a background alarm run today is described plainly`() =
        assertEquals(
            "today 5:02 AM, background alarm: wallpaper updated",
            describeLastCheck(ms(13, 5, 2), "ALARM", "APPLIED", now, ZoneOffset.UTC)
        )

    @Test fun `an older run shows its date`() =
        assertEquals(
            "12 Oct 8:15 PM, you opened the app: wallpaper updated",
            describeLastCheck(ms(12, 20, 15), "APP_OPEN", "APPLIED", now, ZoneOffset.UTC)
        )

    @Test fun `a background job that found it already current is still reported`() =
        assertEquals(
            "today 5:03 AM, background job: already up to date",
            describeLastCheck(ms(13, 5, 3), "WORK", "UP_TO_DATE", now, ZoneOffset.UTC)
        )

    @Test fun `failures say the phone refused`() =
        assertEquals(
            "today 5:00 AM, Update now: the phone refused the change",
            describeLastCheck(ms(13, 5, 0), "MANUAL", "FAILED", now, ZoneOffset.UTC)
        )

    @Test fun `unknown values degrade gracefully instead of crashing`() =
        assertEquals(
            "today 5:00 AM, unknown: weird",
            describeLastCheck(ms(13, 5, 0), "X", "WEIRD", now, ZoneOffset.UTC)
        )
}
