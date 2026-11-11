package com.dailydivine.app.data.content

import android.content.Context
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.local.entity.Verse
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** One verse's text in another language (assets/content_overlays/<religion>_<lang>.json). */
data class OverlayVerse(val id: Int, val dayNumber: Int, val translatedText: String)

/** Shape of an overlay file. The verse ids are the ids of the English content file. */
data class OverlayFile(val religion: String, val language: String, val verses: List<OverlayVerse>)

/**
 * Shows verses in the user's chosen language where a translation exists, WITHOUT a second copy of
 * the verses in the database: the stored verse stays English, and a small in-memory overlay (same
 * verse ids) replaces the text on the way to the screen. A verse with no overlay entry simply
 * stays English, so a missing translation can never produce an empty screen.
 *
 * Currently only Hinduism has one: Hindi meanings of the Bhagavad Gita verses.
 */
@Singleton
class VerseLocalizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) {
    private val cache = HashMap<String, Map<Int, OverlayVerse>>()

    /** The chosen language code, re-emitted when the user changes it. */
    val languageCode: Flow<String> = userPreferences.state.map { it.languageCode }.distinctUntilChanged()

    suspend fun currentLanguage(): String = userPreferences.state.first().languageCode

    fun localize(verse: Verse, lang: String): Verse =
        apply(verse, lang, overlayFor(verse.religionId, lang)?.get(verse.id))

    fun localize(verses: List<Verse>, lang: String): List<Verse> =
        if (lang == "en") verses else verses.map { localize(it, lang) }

    /**
     * When the user reads [religionId] in [lang] and a translation exists, the daily verse is chosen
     * among the translated verses only (so Hindi readers do not get an English verse every other
     * day). Null means: use all verses as usual.
     */
    fun dailyRange(religionId: Int, lang: String): IntRange? =
        overlayFor(religionId, lang)?.values?.let { rangeOf(it) }

    private fun overlayFor(religionId: Int, lang: String): Map<Int, OverlayVerse>? {
        if (lang == "en") return null
        val asset = OVERLAY_ASSETS[religionId to lang] ?: return null
        return synchronized(cache) { cache.getOrPut(asset) { load(asset) } }
    }

    private fun load(asset: String): Map<Int, OverlayVerse> = runCatching {
        context.assets.open(asset).bufferedReader(Charsets.UTF_8).use { r ->
            Gson().fromJson(r, OverlayFile::class.java).verses.associateBy { it.id }
        }
    }.getOrDefault(emptyMap()) // a broken overlay degrades to English, never to a crash

    companion object {
        /** (religion id, language code) -> overlay asset. Add a language here only together with its file. */
        val OVERLAY_ASSETS: Map<Pair<Int, String>, String> = mapOf(
            (1 to "hi") to "content_overlays/hinduism_hi.json"
        )

        fun apply(verse: Verse, lang: String, overlay: OverlayVerse?): Verse =
            if (overlay == null || overlay.translatedText.isBlank()) verse
            else verse.copy(translatedText = overlay.translatedText, languageCode = lang)

        /** First..last day number, or null if there are none or they are not consecutive. */
        fun rangeOf(overlay: Collection<OverlayVerse>): IntRange? {
            if (overlay.isEmpty()) return null
            val days = overlay.map { it.dayNumber }.sorted()
            val consecutive = days.zipWithNext().all { (a, b) -> b == a + 1 }
            return if (consecutive) days.first()..days.last() else null
        }
    }
}
