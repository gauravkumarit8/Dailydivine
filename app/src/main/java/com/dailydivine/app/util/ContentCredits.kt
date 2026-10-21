package com.dailydivine.app.util

/**
 * Where each tradition's text comes from. Shown in Settings > About > Content sources & credits,
 * and mirrored in licenses/CONTENT_SOURCES.md. All scripture texts are public-domain translations;
 * their wording is historical (archaic English).
 */
object ContentCredits {
    val SCRIPTURE: List<Pair<String, String>> = listOf(
        "Hinduism" to "The Song Celestial (Bhagavad-Gita), translated by Sir Edwin Arnold, 1885. Public domain. Project Gutenberg #2388.",
        "Christianity" to "The Holy Bible, King James Version. Public domain in the United States.",
        "Islam" to "The Meaning of the Glorious Koran, translated by Marmaduke Pickthall, 1930. Public domain. Text via Tanzil.net.",
        "Buddhism" to "The Dhammapada, translated by F. Max M\u00fcller, Sacred Books of the East vol. X, 1881. Public domain. Project Gutenberg #2017."
    )
    const val OTHER = "Typeface: Poppins (SIL Open Font License 1.1). Alarm tones: original synthesized audio created for DailyDivine. " +
        "The choice and arrangement of passages is editorial and is not a religious authority."
}
