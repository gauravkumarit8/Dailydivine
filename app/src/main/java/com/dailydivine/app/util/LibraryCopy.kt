package com.dailydivine.app.util

/** Wording used by the Library screen. Pure so it is unit-tested. */
object LibraryCopy {
    fun verseCount(count: Int): String = "$count ${if (count == 1) "verse" else "verses"}"

    /** The letter shown in a category's round badge ("Duty & Action" -> "D"). */
    fun initial(name: String): String =
        name.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "\u2022"
}
