package com.dailydivine.app.util

/** The user's light/dark choice. "system" follows the phone, which is also what every earlier version did. */
object ThemeMode {
    const val SYSTEM = "system"
    const val LIGHT = "light"
    const val DARK = "dark"

    /** Anything unknown or missing means "system". */
    fun normalize(raw: String?): String = if (raw == LIGHT || raw == DARK) raw else SYSTEM

    fun isDark(mode: String?, systemIsDark: Boolean): Boolean = when (normalize(mode)) {
        LIGHT -> false
        DARK -> true
        else -> systemIsDark
    }
}
