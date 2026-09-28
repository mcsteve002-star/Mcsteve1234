package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecurringTransactionEntity): Long

    @Update
    suspend fun update(item: RecurringTransactionEntity)

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId ORDER BY nextDueDateEpochMillis ASC")
    fun getAll(userId: Long): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveSync(userId: Long): List<RecurringTransactionEntity>

    @Query("DELETE FROM recurring_transactions WHERE id = :id AND userId = :userId")
    suspend fun delete(id: Long, userId: Long)

    @Query("DELETE FROM recurring_transactions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}
