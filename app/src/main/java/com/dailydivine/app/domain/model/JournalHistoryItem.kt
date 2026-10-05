package com.dailydivine.app.domain.model

import com.dailydivine.app.data.local.entity.StreakEntry
import com.dailydivine.app.data.local.entity.Verse

/** One past day for the History tab (F006 history, F011-R04): the day's
 *  entry plus the verse shown that day (null only if the verse was since removed). */
data class JournalHistoryItem(val entry: StreakEntry, val verse: Verse?)

/** F011-R05: optional mood selector choices. */
val MOOD_EMOJIS = listOf("\uD83D\uDE0A", "\uD83D\uDE0C", "\uD83D\uDE4F", "\uD83D\uDE14", "\uD83D\uDE1F")

/** Spoken names, same order as [MOOD_EMOJIS] (A11Y-03/05). */
val MOOD_LABELS = listOf("Happy", "Calm", "Grateful", "Sad", "Worried")
