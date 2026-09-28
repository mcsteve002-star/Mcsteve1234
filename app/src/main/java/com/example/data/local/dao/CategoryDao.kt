package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories WHERE userId = 0 OR userId = :userId ORDER BY name ASC")
    fun getAllCategories(userId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE (userId = 0 OR userId = :userId) AND type = :type ORDER BY name ASC")
    fun getCategoriesByType(userId: Long, type: String): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("DELETE FROM categories WHERE id = :id AND userId = :userId AND isCustom = 1")
    suspend fun deleteCustomCategory(id: Long, userId: Long)

    @Query("DELETE FROM categories WHERE userId = :userId AND isCustom = 1")
    suspend fun deleteAllCustomForUser(userId: Long)
}
