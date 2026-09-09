package com.dailydivine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailydivine.app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT * FROM categories WHERE religionId = :religionId ORDER BY sortOrder")
    fun getCategoriesForReligion(religionId: Int): Flow<List<Category>>
}
