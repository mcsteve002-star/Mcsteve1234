package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_reminders",
    indices = [Index(value = ["userId"])]
)
data class BillReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val amount: Double,
    val category: String,
    val dueDateEpochMillis: Long,
    val recurrence: String = "MONTHLY", // "ONCE", "WEEKLY", "MONTHLY"
    val isPaid: Boolean = false,
    val reminderDaysBefore: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
