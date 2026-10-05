package com.dailydivine.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** PRD 20.3 / A11Y: 4.5:1 text contrast in every religion palette, light and dark. */
class ContrastTest {

    private val palettes = (0..6).associateWith { paletteForReligionId(it) }

    @Test
    fun `black on white is the maximum 21 to 1`() {
        assertEquals(21f, contrastRatio(Color.White, Color.Black), 0.01f)
    }

    @Test
    fun `light theme primary passes on white and on the tinted page for every palette`() {
        palettes.forEach { (id, p) ->
            val c = lightSchemePrimary(p)
            assertTrue("palette $id vs white", contrastRatio(c, Color.White) >= MIN_TEXT_CONTRAST)
            assertTrue("palette $id vs surface", contrastRatio(c, p.surface) >= MIN_TEXT_CONTRAST)
        }
    }

    @Test
    fun `dark theme primary passes on every dark container for every palette`() {
        palettes.forEach { (id, p) ->
            val c = darkSchemePrimary(p)
            DARK_SURFACES.forEach { bg ->
                assertTrue("palette $id vs $bg", contrastRatio(c, bg) >= MIN_TEXT_CONTRAST)
            }
        }
    }

    @Test
    fun `palettes that already pass keep their exact brand colour`() {
        listOf(2, 4, 6).forEach { id ->        // Christianity, Buddhism, Judaism measured >= 4.5 on white and surface
            val p = palettes.getValue(id)
            assertEquals("palette $id", p.primary, lightSchemePrimary(p))
        }
    }

    @Test
    fun `palettes that fail are only made darker, never replaced`() {
        listOf(1, 5).forEach { id ->            // Hinduism 3.16:1 and Sikhism 3.79:1 before the fix
            val p = palettes.getValue(id)
            assertTrue("palette $id was failing", contrastRatio(p.primary, Color.White) < MIN_TEXT_CONTRAST)
            assertTrue("palette $id got darker", lightSchemePrimary(p).luminance() < p.primary.luminance())
        }
    }

    @Test
    fun `white-on-primary buttons pass in light theme for every palette`() {
        palettes.forEach { (id, p) ->
            assertTrue("palette $id", contrastRatio(Color.White, lightSchemePrimary(p)) >= MIN_TEXT_CONTRAST)
        }
    }

    @Test
    fun `text on the dark-theme primary is readable for every palette`() {
        palettes.forEach { (id, p) ->
            val primary = darkSchemePrimary(p)
            assertTrue("palette $id", contrastRatio(onColorFor(primary), primary) >= MIN_TEXT_CONTRAST)
        }
    }

    @Test
    fun `onColorFor picks the readable side`() {
        assertEquals(Color.White, onColorFor(Color.Black))
        assertTrue(onColorFor(Color.White) != Color.White)
    }

    @Test
    fun `a colour that already passes is returned untouched`() {
        assertEquals(Color.Black, darkenUntilContrast(Color.Black, listOf(Color.White)))
        assertEquals(Color.White, lightenUntilContrast(Color.White, listOf(Color.Black)))
    }

    @Test
    fun `selected-card tint keeps its text readable for every palette`() {
        // Mirrors Theme.kt: primaryContainer = blend of surface/background and primary.
        palettes.forEach { (id, p) ->
            val light = androidx.compose.ui.graphics.lerp(p.surface, lightSchemePrimary(p), 0.18f)
            assertTrue("light $id", contrastRatio(Color(0xFF1C1B1F), light) >= MIN_TEXT_CONTRAST)
            val dark = androidx.compose.ui.graphics.lerp(Color(0xFF1C1B1F), darkSchemePrimary(p), 0.30f)
            assertTrue("dark $id", contrastRatio(Color.White, dark) >= MIN_TEXT_CONTRAST)
        }
    }
}
