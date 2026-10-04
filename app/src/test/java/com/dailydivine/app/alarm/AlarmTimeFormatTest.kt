package com.dailydivine.app.alarm

import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmTimeFormatTest {
    private val min = 60_000L
    private val hour = 60 * min

    @Test fun `under a minute`() {
        assertEquals("Rings in less than a minute", formatRingsIn(0))
        assertEquals("Rings in less than a minute", formatRingsIn(59_000))
        assertEquals("Rings in less than a minute", formatRingsIn(-5_000)) // never negative text
    }

    @Test fun `minutes`() {
        assertEquals("Rings in 1 min", formatRingsIn(min))
        assertEquals("Rings in 25 min", formatRingsIn(25 * min))
        assertEquals("Rings in 59 min", formatRingsIn(59 * min + 30_000))
    }

    @Test fun `hours with and without minutes`() {
        assertEquals("Rings in 1h", formatRingsIn(hour))
        assertEquals("Rings in 7h 30m", formatRingsIn(7 * hour + 30 * min))
        assertEquals("Rings in 23h 59m", formatRingsIn(24 * hour - min))
    }

    @Test fun `days`() {
        assertEquals("Rings in 1d", formatRingsIn(24 * hour))
        assertEquals("Rings in 1d 2h", formatRingsIn(26 * hour))
        assertEquals("Rings in 2d 12h", formatRingsIn(60 * hour))
    }
}
