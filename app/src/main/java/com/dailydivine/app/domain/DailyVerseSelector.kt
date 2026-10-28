package com.dailydivine.app.domain

/**
 * Which stored verse (1..total) is "today's" for an install.
 *
 * Uses floorMod so a device clock that moves earlier than the install date (travel, manual
 * change, restored phone) still maps to a valid verse; plain `%` goes negative there and
 * produced dayNumber <= 0, i.e. an empty Home screen.
 */
object DailyVerseSelector {
    fun dayNumber(daysSinceInstall: Long, totalVerses: Int): Int {
        require(totalVerses > 0) { "totalVerses must be positive" }
        return Math.floorMod(daysSinceInstall, totalVerses.toLong()).toInt() + 1
    }
}
