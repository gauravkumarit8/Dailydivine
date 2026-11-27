package com.dailydivine.app.data.content

/**
 * One passage of the Bhagavad Gita reader (assets/gita/gita_reader.json): a verse, or a group of verses that
 * Besant translates together. [id] is "chapter.firstVerse" in the standard 700-verse numbering ("13.0" is
 * Arjuna's question that opens chapter 13). [meaningEn] / [meaningHi] exist only for the verses that have
 * DailyDivine's own plain-words meanings.
 */
data class GitaPassage(
    val id: String,
    val label: String,
    val sanskrit: String,
    val english: String,
    val speaker: String? = null,
    val meaningEn: String? = null,
    val meaningHi: String? = null
)

data class GitaChapter(val number: Int, val title: String, val passages: List<GitaPassage>)

data class GitaBook(val version: Int = 0, val source: String = "", val chapters: List<GitaChapter> = emptyList())
