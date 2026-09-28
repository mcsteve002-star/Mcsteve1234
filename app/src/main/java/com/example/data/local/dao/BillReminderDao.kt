package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BillReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillReminderEntity): Long

    @Update
    suspend fun updateBill(bill: BillReminderEntity)

    @Query("SELECT * FROM bill_reminders WHERE userId = :userId ORDER BY isPaid ASC, dueDateEpochMillis ASC")
    fun getAllBills(userId: Long): Flow<List<BillReminderEntity>>

    @Query("SELECT * FROM bill_reminders WHERE userId = :userId AND isPaid = 0 AND dueDateEpochMillis <= :dueThreshold")
    suspend fun getUpcomingUnpaidBillsSync(userId: Long, dueThreshold: Long): List<BillReminderEntity>

    @Query("UPDATE bill_reminders SET isPaid = :isPaid WHERE id = :id AND userId = :userId")
    suspend fun setBillPaidStatus(id: Long, userId: Long, isPaid: Boolean)

    @Query("DELETE FROM bill_reminders WHERE id = :id AND userId = :userId")
    suspend fun deleteBill(id: Long, userId: Long)

    @Query("DELETE FROM bill_reminders WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}
