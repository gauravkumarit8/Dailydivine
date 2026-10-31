package com.dailydivine.app.util

/**
 * How a verse is shown on screen. Pure (no Compose/Android) so it is unit-tested.
 *
 * Long verses (the Gita and Dhammapada passages are 6 to 10 lines) were set at one fixed size,
 * so on a phone every poem line wrapped and the card filled the whole screen.
 */
object VerseDisplayRules {

    private const val EM_DASH = '\u2014'
    private const val OPEN_SINGLE = '\u2018'
    private const val CLOSE_SINGLE = '\u2019'

    /**
     * Typography for display only (stored text is untouched):
     *  - Project Gutenberg's "--" becomes a real em dash;
     *  - straight quotes become curly. The screen already wraps the whole verse in curly DOUBLE
     *    quotes, so quotes inside it are shown as curly SINGLE quotes (a straight " inside curly
     *    quotes looked wrong on the phone). An apostrophe or closing quote is the right-hand mark.
     */
    fun normalize(text: String): String {
        val dashed = text.replace("--", EM_DASH.toString())
        val out = StringBuilder(dashed.length)
        for (i in dashed.indices) {
            val c = dashed[i]
            if (c == '"' || c == '\'') {
                val prev = if (i == 0) ' ' else dashed[i - 1]
                val opening = prev.isWhitespace() || prev == '(' || prev == '[' || prev == EM_DASH
                out.append(if (opening) OPEN_SINGLE else CLOSE_SINGLE)
            } else {
                out.append(c)
            }
        }
        return out.toString()
    }

    /** Font size in sp for the hero verse: shorter verses are larger, long ones shrink to stay readable. */
    fun fontSizeSp(length: Int): Int = when {
        length <= 140 -> 24
        length <= 260 -> 21
        else -> 18
    }
}
