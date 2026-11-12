package com.dailydivine.app.util

/**
 * Phone makers add their own background-app limits on top of Android's, and the standard
 * "Don't optimize" list does not lift them. This returns the extra step for the makers whose
 * limits are best known to stop daily alarms and wallpaper changes, or null when the standard
 * step is all we can say. Menu names change between versions, so the text says so.
 */
object BatteryGuidance {
    fun extraStep(manufacturer: String?): String? = when (manufacturer?.trim()?.lowercase()) {
        "xiaomi", "redmi", "poco" ->
            "On Xiaomi, Redmi and POCO phones also open Settings > Apps > Manage apps > DailyDivine, " +
                "turn on Autostart, and set Battery saver to No restrictions. Menu names can differ slightly by version."
        "samsung" ->
            "On Samsung phones also add DailyDivine to Settings > Battery > Background usage limits > Never sleeping apps. " +
                "Menu names can differ slightly by version."
        else -> null
    }

    /** [base] followed by the maker-specific step, if there is one. */
    fun withExtra(base: String, manufacturer: String?): String =
        extraStep(manufacturer)?.let { "$base\n\n$it" } ?: base
}
