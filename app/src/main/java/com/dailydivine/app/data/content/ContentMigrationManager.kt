package com.dailydivine.app.data.content

import com.dailydivine.app.data.local.dao.CategoryDao
import com.dailydivine.app.data.local.dao.ContentVersionDao
import com.dailydivine.app.data.local.dao.VerseDao
import com.dailydivine.app.data.local.entity.ContentVersion
import com.google.gson.Gson

/**
 * PRD F002-R13 (v1.1, NEW).
 *
 * On first install, [migrateIfNeeded] does a full insert (nothing exists
 * yet -> "diff" against an empty ID set is just "insert everything").
 *
 * On every subsequent app launch (in particular after an app update that
 * ships a bumped content JSON), it compares the bundled JSON's `version`
 * field against `content_versions.appliedVersion` for that religion. If the
 * bundled version is higher, it diffs verse IDs and inserts ONLY the
 * new/changed ones.
 *
 * It never deletes or truncates the verses/streaks/bookmarks/user_profile
 * tables. That is the entire point of this class: an app update must not be
 * able to destroy a user's streak or bookmarks.
 */
class ContentMigrationManager(
    private val contentLoader: ContentLoader,
    private val verseDao: VerseDao,
    private val categoryDao: CategoryDao,
    private val contentVersionDao: ContentVersionDao,
    private val gson: Gson = Gson()
) {

    data class MigrationResult(
        val religionId: Int,
        val fromVersion: Int,
        val toVersion: Int,
        val versesAdded: Int
    )

    /**
     * @param religionId       Room primary key for the religion
     * @param assetFileName    e.g. "hinduism_en.json"
     * @param languageCode     e.g. "en"
     * @return null if already up to date (no-op — the common case on most launches)
     */
    suspend fun migrateIfNeeded(
        religionId: Int,
        assetFileName: String,
        languageCode: String
    ): MigrationResult? {
        val file = contentLoader.loadContentFile(religionId, assetFileName, gson)
        val stored = contentVersionDao.get(religionId)
        val fromVersion = stored?.appliedVersion ?: 0

        if (file.version <= fromVersion) {
            return null // already current — nothing to do, nothing touched
        }

        // Diff: only insert verses whose ID isn't already in the DB.
        val existingIds = verseDao.getExistingVerseIds(religionId).toHashSet()
        val allVerses = contentLoader.toVerseEntities(religionId, file, languageCode)
        val newVerses = allVerses.filter { it.id !in existingIds }

        if (newVerses.isNotEmpty()) {
            verseDao.insertAll(newVerses) // OnConflictStrategy.IGNORE — belt-and-braces safety
        }
        categoryDao.insertAll(contentLoader.toCategoryEntities(religionId, file))

        contentVersionDao.upsert(
            ContentVersion(
                religionId = religionId,
                appliedVersion = file.version,
                lastMigratedAt = System.currentTimeMillis()
            )
        )

        return MigrationResult(
            religionId = religionId,
            fromVersion = fromVersion,
            toVersion = file.version,
            versesAdded = newVerses.size
        )
        // Deliberately absent: any DELETE / truncate of verses, streaks,
        // bookmarks, or user_profile. That absence is the fix for F002-R13.
    }
}
