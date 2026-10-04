package com.dailydivine.app.alarm

/** "Rings in 7h 30m" for the alarm card. Pure so it can be unit-tested. */
fun formatRingsIn(millisUntil: Long): String {
    val totalMinutes = (millisUntil / 60_000L).coerceAtLeast(0L)
    return when {
        totalMinutes < 1 -> "Rings in less than a minute"
        totalMinutes < 60 -> "Rings in $totalMinutes min"
        totalMinutes < 24 * 60 -> {
            val h = totalMinutes / 60
            val m = totalMinutes % 60
            if (m == 0L) "Rings in ${h}h" else "Rings in ${h}h ${m}m"
        }
        else -> {
            val d = totalMinutes / (24 * 60)
            val h = (totalMinutes % (24 * 60)) / 60
            if (h == 0L) "Rings in ${d}d" else "Rings in ${d}d ${h}h"
        }
    }
}
