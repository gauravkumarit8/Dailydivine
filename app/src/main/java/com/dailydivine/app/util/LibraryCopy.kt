package com.dailydivine.app.util

/** Wording used by the Library screen. Pure so it is unit-tested. */
object LibraryCopy {
    fun verseCount(count: Int): String = "$count ${if (count == 1) "verse" else "verses"}"

    /** Two letters for the round badge ("Duty & Action" -> "Du", "Devotion & Virtue" -> "De"): one letter was not enough, several categories start alike. */
    fun initials(name: String): String {
        val letters = name.filter { it.isLetterOrDigit() }.take(2)
        return when (letters.length) {
            0 -> "\u2022"
            1 -> letters.uppercase()
            else -> letters.first().uppercaseChar().toString() + letters[1].lowercaseChar()
        }
    }

    /** The letter shown in a category's round badge ("Duty & Action" -> "D"). */
    fun initial(name: String): String =
        name.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "\u2022"
}
