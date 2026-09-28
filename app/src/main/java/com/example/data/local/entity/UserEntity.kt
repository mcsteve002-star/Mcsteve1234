package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val phoneNumber: String = "",
    val country: String = "Ghana",
    val preferredCurrency: String = "GHS",
    val monthlyIncome: Double = 0.0,
    val isEmailVerified: Boolean = true,
    val preferredBudgetPeriod: String = "MONTHLY",
    val morningReminderHour: Int = 8,
    val morningReminderMinute: Int = 0,
    val eveningReminderHour: Int = 20,
    val eveningReminderMinute: Int = 0,
    val dailyRemindersEnabled: Boolean = true,
    val budgetAlertsEnabled: Boolean = true,
    val billRemindersEnabled: Boolean = true,
    val appLockPin: String? = null,
    val isBiometricEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
