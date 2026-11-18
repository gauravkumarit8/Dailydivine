package com.dailydivine.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test fun `system follows the phone`() {
        assertTrue(ThemeMode.isDark("system", systemIsDark = true))
        assertFalse(ThemeMode.isDark("system", systemIsDark = false))
    }

    @Test fun `light and dark ignore the phone`() {
        assertFalse(ThemeMode.isDark("light", systemIsDark = true))
        assertTrue(ThemeMode.isDark("dark", systemIsDark = false))
    }

    @Test fun `unknown or missing values mean system`() {
        assertEquals("system", ThemeMode.normalize(null))
        assertEquals("system", ThemeMode.normalize("purple"))
        assertTrue(ThemeMode.isDark(null, systemIsDark = true))
    }
}
