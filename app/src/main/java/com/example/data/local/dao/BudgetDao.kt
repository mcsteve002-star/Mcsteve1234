package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getAllBudgets(userId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND period = :period AND category IS NULL LIMIT 1")
    fun getOverallBudget(userId: Long, period: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND period = :period AND category IS NULL LIMIT 1")
    suspend fun getOverallBudgetSync(userId: Long, period: String): BudgetEntity?

    @Query("DELETE FROM budgets WHERE id = :id AND userId = :userId")
    suspend fun deleteBudget(id: Long, userId: Long)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}
