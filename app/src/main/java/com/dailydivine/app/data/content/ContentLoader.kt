package com.dailydivine.app.data.content

import android.content.Context
import com.dailydivine.app.data.local.entity.Category
import com.dailydivine.app.data.local.entity.Verse
import com.google.gson.annotations.SerializedName

/** Shape of assets/content/<religion>_<lang>.json (PRD Section 11.2). */
data class ContentFile(
    val religion: String,
    val language: String,
    val version: Int,
    val categories: List<CategoryJson>,
    val verses: List<VerseJson>
)

data class CategoryJson(
    val id: Int,
    val name: String,
    val icon: String,
    val verseCount: Int
)

data class VerseJson(
    val id: Int,
    val categoryId: Int,
    val dayNumber: Int,
    val originalText: String,
    val translatedText: String,
    val sourceReference: String,
    val tags: String? = null,
    @SerializedName("audioFilePath") val audioFilePath: String? = null,
    @SerializedName("isPremium") val isPremium: Boolean = false
)

/**
 * Reads and parses a religion's content JSON from assets/content/.
 * Pure parsing only — inserting into Room is handled by the repository /
 * ContentMigrationManager so this class stays easily unit-testable without
 * a database.
 */
class ContentLoader(private val context: Context) {

    fun loadContentFile(religionId: Int, assetFileName: String, gson: com.google.gson.Gson): ContentFile {
        val json = context.assets.open("content/$assetFileName").bufferedReader().use { it.readText() }
        return gson.fromJson(json, ContentFile::class.java)
    }

    fun toVerseEntities(religionId: Int, file: ContentFile, languageCode: String): List<Verse> =
        file.verses.map { v ->
            Verse(
                id = v.id,
                religionId = religionId,
                categoryId = v.categoryId,
                dayNumber = v.dayNumber,
                originalText = v.originalText,
                translatedText = v.translatedText,
                sourceReference = v.sourceReference,
                languageCode = languageCode,
                audioFilePath = v.audioFilePath,
                isPremium = v.isPremium,
                tags = v.tags
            )
        }

    fun toCategoryEntities(religionId: Int, file: ContentFile): List<Category> =
        file.categories.mapIndexed { index, c ->
            Category(
                id = c.id,
                religionId = religionId,
                name = c.name,
                icon = c.icon,
                description = "",
                verseCount = c.verseCount,
                sortOrder = index,
                isPremium = false
            )
        }
}
