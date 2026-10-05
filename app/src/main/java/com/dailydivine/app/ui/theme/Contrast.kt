package com.dailydivine.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/**
 * WCAG 2.x contrast helpers (PRD 20.3 / A11Y: 4.5:1 minimum for text).
 * The PRD's religion colours were chosen for looks: Hinduism's orange is only
 * 3.16:1 against white and Sikhism's 3.79:1, so white-on-primary buttons and
 * primary-coloured labels failed the PRD's own requirement. Rather than
 * replacing the brand colours, they are nudged just far enough to pass, and a
 * colour that already passes is returned unchanged.
 */
const val MIN_TEXT_CONTRAST = 4.5f

fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}

private fun passes(c: Color, backgrounds: List<Color>, min: Float) =
    backgrounds.all { contrastRatio(c, it) >= min }

/** Darkens [color] in small steps until it reaches [min] contrast on every background. */
fun darkenUntilContrast(color: Color, backgrounds: List<Color>, min: Float = MIN_TEXT_CONTRAST): Color {
    var c = color
    var steps = 0
    while (!passes(c, backgrounds, min) && steps < 60) {
        c = lerp(c, Color.Black, 0.05f)
        steps++
    }
    return c
}

/** Lightens [color] until it reaches [min] contrast on every (dark) background. */
fun lightenUntilContrast(color: Color, backgrounds: List<Color>, min: Float = MIN_TEXT_CONTRAST): Color {
    var c = color
    var steps = 0
    while (!passes(c, backgrounds, min) && steps < 60) {
        c = lerp(c, Color.White, 0.05f)
        steps++
    }
    return c
}

/** White or near-black, whichever reads better on [background]. */
fun onColorFor(background: Color): Color {
    val dark = Color(0xFF1C1B1F)
    return if (contrastRatio(Color.White, background) >= contrastRatio(dark, background)) Color.White else dark
}

/** Material 3's default dark container colours: text on them must also pass. */
val DARK_SURFACES = listOf(Color(0xFF1C1B1F), Color(0xFF36343B), Color(0xFF49454F))

/** Light theme: primary is used as white-on-primary AND as text on white cards / the tinted page. */
fun lightSchemePrimary(p: ReligionPalette): Color =
    darkenUntilContrast(p.primary, listOf(Color.White, p.surface))

/** Dark theme: primary is used as text on the dark surfaces. */
fun darkSchemePrimary(p: ReligionPalette): Color =
    lightenUntilContrast(p.primary, DARK_SURFACES)
