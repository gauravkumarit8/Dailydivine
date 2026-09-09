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

    @Query("SELECT * FROM streaks WHERE date = :date LIMIT 1")
    suspend fun getEntryForDate(date: String): StreakEntry?

    @Query("SELECT * FROM streaks ORDER BY date DESC")
    fun getAllEntries(): Flow<List<StreakEntry>>

    @Query("SELECT * FROM streaks ORDER BY date DESC LIMIT :days")
    fun getRecentEntries(days: Int): Flow<List<StreakEntry>>

    @Query("SELECT COUNT(*) FROM streaks WHERE date >= :startDate")
    suspend fun getStreakCount(startDate: String): Int
}
