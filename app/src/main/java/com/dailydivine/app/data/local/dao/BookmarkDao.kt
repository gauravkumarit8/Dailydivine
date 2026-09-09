package com.dailydivine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailydivine.app.data.local.entity.Verse
import com.dailydivine.app.data.local.entity.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE verseId = :verseId")
    suspend fun removeBookmark(verseId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE verseId = :verseId)")
    fun isBookmarked(verseId: Int): Flow<Boolean>

    @Query(
        """SELECT v.* FROM verses v INNER JOIN bookmarks b
           ON v.id = b.verseId ORDER BY b.createdAt DESC"""
    )
    fun getBookmarkedVerses(): Flow<List<Verse>>
}
