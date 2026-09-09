package com.dailydivine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailydivine.app.data.local.entity.AlarmTone

@Dao
interface AlarmToneDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tones: List<AlarmTone>)

    @Query("SELECT * FROM alarm_tones WHERE religionId = :religionId OR religionId IS NULL")
    suspend fun getTonesForReligion(religionId: Int): List<AlarmTone>
}
