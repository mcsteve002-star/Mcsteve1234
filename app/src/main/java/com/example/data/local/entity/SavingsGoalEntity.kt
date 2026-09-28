package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_goals",
    indices = [Index(value = ["userId"])]
)
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val targetAmount: Double,
    val currentSaved: Double = 0.0,
    val targetDateEpochMillis: Long,
    val note: String = "",
    val colorHex: String = "#00897B",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
