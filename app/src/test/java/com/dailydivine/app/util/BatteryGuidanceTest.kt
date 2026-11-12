package com.dailydivine.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryGuidanceTest {
    @Test fun `xiaomi family gets the autostart step whatever the capitalisation`() {
        for (m in listOf("Xiaomi", "XIAOMI", "Redmi", "POCO", " xiaomi ")) {
            assertTrue(m, BatteryGuidance.extraStep(m)!!.contains("Autostart"))
        }
    }

    @Test fun `samsung gets its own step`() = assertTrue(BatteryGuidance.extraStep("samsung")!!.contains("Never sleeping apps"))

    @Test fun `unknown makers and missing values get nothing extra`() {
        assertNull(BatteryGuidance.extraStep("Google"))
        assertNull(BatteryGuidance.extraStep(""))
        assertNull(BatteryGuidance.extraStep(null))
        assertEquals("base", BatteryGuidance.withExtra("base", "Google"))
    }

    @Test fun `the extra step is appended after the base text`() {
        val out = BatteryGuidance.withExtra("base", "Xiaomi")
        assertTrue(out.startsWith("base\n\n"))
        assertNotNull(BatteryGuidance.extraStep("Xiaomi"))
    }
}
