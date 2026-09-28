package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["dateEpochMillis"]),
        Index(value = ["category"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val type: String, // "EXPENSE" or "INCOME"
    val amount: Double,
    val category: String,
    val description: String,
    val paymentMethod: String = "Cash", // "Cash", "MTN MoMo", "Telecel Cash", "AT Money", "Bank Transfer", "Debit/Credit Card", "Other"
    val dateEpochMillis: Long,
    val timeString: String = "12:00",
    val note: String = "",
    val receiptUri: String? = null,
    val location: String? = null,
    val isRecurring: Boolean = false,
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis()
)
