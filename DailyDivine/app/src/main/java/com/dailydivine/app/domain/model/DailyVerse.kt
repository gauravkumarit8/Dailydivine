package com.dailydivine.app.domain.model

import com.dailydivine.app.data.local.entity.Verse

data class DailyVerse(
    val verse: Verse,
    val dayNumber: Int,
    val isBookmarked: Boolean,
    val hasJournalEntry: Boolean
)
