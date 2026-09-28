package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllGoals(userId: Long): Flow<List<SavingsGoalEntity>>

    @Query("UPDATE savings_goals SET currentSaved = currentSaved + :amount WHERE id = :id AND userId = :userId")
    suspend fun addContribution(id: Long, userId: Long, amount: Double)

    @Query("DELETE FROM savings_goals WHERE id = :id AND userId = :userId")
    suspend fun deleteGoal(id: Long, userId: Long)

    @Query("DELETE FROM savings_goals WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}
