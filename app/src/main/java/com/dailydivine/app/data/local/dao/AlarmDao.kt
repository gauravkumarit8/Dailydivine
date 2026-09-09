package com.dailydivine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dailydivine.app.data.local.entity.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm?
}
