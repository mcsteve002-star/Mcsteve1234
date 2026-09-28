package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BillReminderDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.RecurringTransactionDao
import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.BillReminderEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        RecurringTransactionEntity::class,
        BillReminderEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CediWiseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: CediWiseDatabase? = null

        fun getDatabase(context: Context): CediWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CediWiseDatabase::class.java,
                    "cediwise_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultCategories(database.categoryDao())
                }
            }
        }
    }
}

suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
    if (categoryDao.getCategoryCount() > 0) return

    val defaultExpenseCategories = listOf(
        CategoryEntity(name = "Food & Dining", type = "EXPENSE", iconName = "Restaurant", colorHex = "#EF4444"),
        CategoryEntity(name = "Trotro / Transport", type = "EXPENSE", iconName = "DirectionsBus", colorHex = "#F59E0B"),
        CategoryEntity(name = "Mobile Money (MoMo)", type = "EXPENSE", iconName = "AccountBalanceWallet", colorHex = "#EAB308"),
        CategoryEntity(name = "Airtime & Data", type = "EXPENSE", iconName = "Wifi", colorHex = "#3B82F6"),
        CategoryEntity(name = "Fuel & Vehicle", type = "EXPENSE", iconName = "LocalGasStation", colorHex = "#EC4899"),
        CategoryEntity(name = "Rent & Housing", type = "EXPENSE", iconName = "Home", colorHex = "#8B5CF6"),
        CategoryEntity(name = "Utilities (Electricity/Water)", type = "EXPENSE", iconName = "Bolt", colorHex = "#6366F1"),
        CategoryEntity(name = "School Fees & Education", type = "EXPENSE", iconName = "School", colorHex = "#14B8A6"),
        CategoryEntity(name = "Healthcare & Medication", type = "EXPENSE", iconName = "MedicalServices", colorHex = "#10B981"),
        CategoryEntity(name = "Church / Tithes / Donations", type = "EXPENSE", iconName = "VolunteerActivism", colorHex = "#F97316"),
        CategoryEntity(name = "Family Support", type = "EXPENSE", iconName = "Diversity1", colorHex = "#D946EF"),
        CategoryEntity(name = "Shopping & Groceries", type = "EXPENSE", iconName = "ShoppingCart", colorHex = "#06B6D4"),
        CategoryEntity(name = "Entertainment & Outings", type = "EXPENSE", iconName = "Nightlife", colorHex = "#84CC16"),
        CategoryEntity(name = "Personal Care", type = "EXPENSE", iconName = "Spa", colorHex = "#F43F5E"),
        CategoryEntity(name = "Subscriptions", type = "EXPENSE", iconName = "Subscriptions", colorHex = "#64748B"),
        CategoryEntity(name = "Business & Supplies", type = "EXPENSE", iconName = "Storefront", colorHex = "#0EA5E9"),
        CategoryEntity(name = "Other Expenses", type = "EXPENSE", iconName = "Category", colorHex = "#94A3B8")
    )

    val defaultIncomeCategories = listOf(
        CategoryEntity(name = "Salary & Wages", type = "INCOME", iconName = "Work", colorHex = "#10B981"),
        CategoryEntity(name = "Business / Sales", type = "INCOME", iconName = "Payments", colorHex = "#059669"),
        CategoryEntity(name = "Freelance & Gig", type = "INCOME", iconName = "Laptop", colorHex = "#0D9488"),
        CategoryEntity(name = "Mobile Money Inflow", type = "INCOME", iconName = "PhoneAndroid", colorHex = "#F59E0B"),
        CategoryEntity(name = "Allowance", type = "INCOME", iconName = "CardGiftcard", colorHex = "#6366F1"),
        CategoryEntity(name = "Gifts & Donations", type = "INCOME", iconName = "Redeem", colorHex = "#EC4899"),
        CategoryEntity(name = "Investment & Dividends", type = "INCOME", iconName = "TrendingUp", colorHex = "#3B82F6"),
        CategoryEntity(name = "Other Income", type = "INCOME", iconName = "AttachMoney", colorHex = "#14B8A6")
    )

    categoryDao.insertCategories(defaultExpenseCategories + defaultIncomeCategories)
}
