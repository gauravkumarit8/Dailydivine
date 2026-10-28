package com.dailydivine.app.util

/**
 * How a verse is shown on screen. Pure (no Compose/Android) so it is unit-tested.
 *
 * Long verses (the Gita and Dhammapada passages are 6 to 10 lines) were set at one fixed size,
 * so on a phone every poem line wrapped and the card filled the whole screen.
 */
object VerseDisplayRules {

    /** Project Gutenberg texts use "--" for an em dash; show a real dash. */
    fun normalize(text: String): String = text.replace("--", "\u2014")

    /** Font size in sp for the hero verse: shorter verses are larger, long ones shrink to stay readable. */
    fun fontSizeSp(length: Int): Int = when {
        length <= 140 -> 24
        length <= 260 -> 21
        else -> 18
    }
}
