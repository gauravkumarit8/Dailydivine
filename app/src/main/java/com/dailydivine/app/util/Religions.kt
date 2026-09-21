package com.dailydivine.app.util

/**
 * Single source of truth for religion IDs, matching data/local/entity/Religion.kt
 * rows and the ui/theme/Color.kt palette mapping. Centralized here so
 * onboarding, content loading, and theming never drift out of sync with
 * each other (previously each screen had its own hardcoded religion list).
 */
data class ReligionMeta(
    val id: Int,
    val name: String,
    val tagline: String,
    /** assets/content/<file> — null where no sample content exists yet
     *  (only Hinduism has a real sample JSON as of Sprint 1; PRD Section 11.3
     *  needs 11 files total, out of scope until real content is authored). */
    val contentAssetEn: String?,
    val languages: List<Pair<String, String>> // (display name, code)
)

object Religions {
    val ALL = listOf(
        ReligionMeta(
            id = 1, name = "Hinduism", tagline = "Ancient wisdom of the Vedas",
            contentAssetEn = "hinduism_en.json",
            languages = listOf("English" to "en", "Hindi" to "hi", "Sanskrit" to "sa", "Tamil" to "ta", "Telugu" to "te", "Kannada" to "kn")
        ),
        ReligionMeta(
            id = 2, name = "Christianity", tagline = "Words of Christ & Scripture",
            contentAssetEn = null,
            languages = listOf("English" to "en", "Spanish" to "es", "Portuguese" to "pt", "French" to "fr", "Korean" to "ko")
        ),
        ReligionMeta(
            id = 3, name = "Islam", tagline = "Guidance from the Quran",
            contentAssetEn = null,
            languages = listOf("English" to "en", "Arabic" to "ar", "Urdu" to "ur", "Turkish" to "tr", "Malay" to "ms")
        ),
        ReligionMeta(
            id = 4, name = "Buddhism", tagline = "Path to enlightenment",
            contentAssetEn = null,
            languages = listOf("English" to "en", "Thai" to "th", "Japanese" to "ja", "Sinhala" to "si")
        ),
        ReligionMeta(
            id = 5, name = "Sikhism", tagline = "Teachings of the Gurus",
            contentAssetEn = null,
            languages = listOf("English" to "en", "Punjabi" to "pa", "Hindi" to "hi")
        ),
        ReligionMeta(
            id = 6, name = "Judaism", tagline = "Torah & ancient wisdom",
            contentAssetEn = null,
            languages = listOf("English" to "en", "Hebrew" to "he")
        ),
        ReligionMeta(
            id = 7, name = "Spiritual", tagline = "Universal wisdom & peace",
            contentAssetEn = null,
            languages = listOf("English" to "en", "Hindi" to "hi", "Spanish" to "es")
        )
    )

    fun byId(id: Int): ReligionMeta? = ALL.find { it.id == id }
}
