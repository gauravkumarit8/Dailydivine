package com.dailydivine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailydivine.app.data.local.entity.Verse
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {
    @Query("SELECT * FROM verses WHERE religionId = :religionId AND dayNumber = :dayNumber LIMIT 1")
    suspend fun getDailyVerse(religionId: Int, dayNumber: Int): Verse?

    @Query("SELECT * FROM verses WHERE id = :id LIMIT 1")
    suspend fun getVerseById(id: Int): Verse?

    @Query("SELECT * FROM verses WHERE categoryId = :categoryId ORDER BY dayNumber")
    fun getVersesByCategory(categoryId: Int): Flow<List<Verse>>

    @Query(
        """SELECT * FROM verses WHERE religionId = :religionId
           AND (originalText LIKE '%' || :query || '%'
           OR translatedText LIKE '%' || :query || '%'
           OR sourceReference LIKE '%' || :query || '%')"""
    )
    fun searchVerses(religionId: Int, query: String): Flow<List<Verse>>

    @Query("SELECT * FROM verses WHERE religionId = :religionId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerse(religionId: Int): Verse?

    @Query("SELECT COUNT(*) FROM verses WHERE religionId = :religionId")
    suspend fun getVerseCount(religionId: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(verses: List<Verse>)

    // v1.1 — used by ContentMigrationManager (F002-R13) to diff-insert only
    // new verses on a content update, never truncating this table.
    @Query("SELECT id FROM verses WHERE religionId = :religionId")
    suspend fun getExistingVerseIds(religionId: Int): List<Int>

    /**
     * Content updates may correct a verse's original-language text (the Sanskrit) and its reference line.
     * Only those two columns, only where they differ; the verse's id, day, category and translated
     * text are never touched, so streaks, bookmarks and the journal keep pointing at the same verse.
     */
    @Query("UPDATE verses SET originalText = :originalText, sourceReference = :sourceReference " +
        "WHERE id = :id AND (originalText != :originalText OR sourceReference != :sourceReference)")
    suspend fun refreshOriginalAndReference(id: Int, originalText: String, sourceReference: String)
}
