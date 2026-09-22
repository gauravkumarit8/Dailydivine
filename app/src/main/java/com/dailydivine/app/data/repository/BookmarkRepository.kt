package com.dailydivine.app.data.repository

import com.dailydivine.app.data.local.dao.BookmarkDao
import com.dailydivine.app.data.local.entity.Bookmark
import com.dailydivine.app.data.local.entity.Verse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** F007: Bookmarks & Favorites. */
class BookmarkRepository @Inject constructor(private val bookmarkDao: BookmarkDao) {

    fun isBookmarked(verseId: Int): Flow<Boolean> = bookmarkDao.isBookmarked(verseId)

    fun getBookmarkedVerses(): Flow<List<Verse>> = bookmarkDao.getBookmarkedVerses()

    suspend fun toggle(verseId: Int, currentlyBookmarked: Boolean) {
        if (currentlyBookmarked) {
            bookmarkDao.removeBookmark(verseId)
        } else {
            bookmarkDao.addBookmark(Bookmark(verseId = verseId, createdAt = System.currentTimeMillis()))
        }
    }
}
