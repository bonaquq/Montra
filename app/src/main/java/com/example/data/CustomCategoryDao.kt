package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CustomCategory>>

    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    suspend fun getAllCategoriesOnce(): List<CustomCategory>

    @Query("SELECT COUNT(*) FROM custom_categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CustomCategory>)

    @Delete
    suspend fun deleteCategory(category: CustomCategory)

    @Query("DELETE FROM custom_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)
}
