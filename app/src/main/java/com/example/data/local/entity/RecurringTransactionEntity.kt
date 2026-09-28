package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_transactions",
    indices = [Index(value = ["userId"])]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val type: String, // "EXPENSE" or "INCOME"
    val amount: Double,
    val category: String,
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val nextDueDateEpochMillis: Long,
    val paymentMethod: String = "Cash",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
