package com.dailydivine.app.util

/** Formats a 24-hour hour/minute pair as "5:30 AM" style text (F004-R07:
 *  "Time picker (12/24 hour format based on device)" -- 12-hour for now,
 *  device-format detection is a follow-up). */
fun formatTime12h(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(displayHour, minute, period)
}
