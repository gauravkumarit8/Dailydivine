package com.dailydivine.app.util

/**
 * The Bhagavad Gita verses are public-domain Sanskrit with English and Hindi meanings written for
 * DailyDivine, so the line under a Gita verse says whose meaning the reader is looking at (the
 * Arnold, Muller, Pickthall and KJV passages already name their translator in their reference).
 */
object VerseAttribution {
    private const val GITA = "Bhagavad Gita"

    fun line(sourceReference: String, languageCode: String): String =
        if (sourceReference.startsWith(GITA)) {
            val language = if (languageCode == "hi") "Hindi" else "English"
            "$sourceReference \u00B7 $language meaning by DailyDivine"
        } else {
            sourceReference
        }
}
