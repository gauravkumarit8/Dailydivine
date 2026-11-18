package com.dailydivine.app.util

/**
 * The short line of today's verse shown on the alarm screen. It used to stop at ". ! ?" only, so a
 * Hindi verse (which ends with a danda) was cut at 120 characters in the middle of a word
 * ("...आत्मा ही अपन...").
 */
object VersePreview {
    private val SENTENCE_END = charArrayOf('.', '!', '?', '\u0964', '\u0965')

    fun firstSentence(rawText: String, maxChars: Int = 170): String {
        // Verse text keeps its poetic line breaks; flatten them for this short preview.
        val text = rawText.replace(Regex("\\s*\\n\\s*"), " ").trim()
        val end = text.indexOfAny(SENTENCE_END)
        val candidate = if (end in 0 until 220) text.substring(0, end + 1) else text
        if (candidate.length <= maxChars) return candidate
        // Cut at the last space before the limit so no word is split, and say so with an ellipsis.
        val cut = candidate.take(maxChars - 1)
        val lastSpace = cut.lastIndexOf(' ')
        return (if (lastSpace > maxChars / 2) cut.substring(0, lastSpace) else cut).trimEnd(',', ';', ' ') + "\u2026"
    }
}
