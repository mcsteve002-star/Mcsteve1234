package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["period"])
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val period: String, // "DAILY", "WEEKLY", "MONTHLY"
    val category: String? = null, // null for overall limit
    val amount: Double,
    val warningThresholdPercent: Int = 80,
    val createdAt: Long = System.currentTimeMillis()
)
