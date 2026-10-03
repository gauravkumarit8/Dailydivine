package com.dailydivine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailydivine.app.data.local.entity.StreakEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreakEntry(entry: StreakEntry)

    /** F011: updates ONLY the reflection fields of an existing day. Deliberately
     *  an UPDATE, not insertStreakEntry: that one is REPLACE and would reset
     *  the row (and any other field) if used for a journal save. */
    @Query("UPDATE streaks SET journalText = :journalText, moodEmoji = :moodEmoji WHERE date = :date")
    suspend fun updateJournal(date: String, journalText: String?, moodEmoji: String?)

    @Query("SELECT * FROM streaks WHERE date = :date LIMIT 1")
    suspend fun getEntryForDate(date: String): StreakEntry?

    @Query("SELECT * FROM streaks ORDER BY date DESC")
    fun getAllEntries(): Flow<List<StreakEntry>>

    @Query("SELECT * FROM streaks ORDER BY date DESC LIMIT :days")
    fun getRecentEntries(days: Int): Flow<List<StreakEntry>>

    @Query("SELECT COUNT(*) FROM streaks WHERE date >= :startDate")
    suspend fun getStreakCount(startDate: String): Int
}
