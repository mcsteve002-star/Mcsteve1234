package com.example.data.repository

import com.example.data.local.CediWiseDatabase
import com.example.data.local.dao.CategorySum
import com.example.data.local.entity.BillReminderEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.util.CurrencyFormatter
import com.example.util.SecurityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CediWiseRepository(private val database: CediWiseDatabase) {
    private val userDao = database.userDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val recurringTransactionDao = database.recurringTransactionDao()
    private val billReminderDao = database.billReminderDao()
    private val categoryDao = database.categoryDao()

    // --- Authentication & User ---
    suspend fun registerUser(
        fullName: String,
        email: String,
        passwordPlain: String,
        phone: String,
        country: String,
        currency: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email already exists."))
        }
        val (isStrong, msg) = SecurityHelper.isPasswordStrong(passwordPlain)
        if (!isStrong) {
            return@withContext Result.failure(Exception(msg))
        }
        val hash = SecurityHelper.hashPassword(passwordPlain)
        val user = UserEntity(
            fullName = fullName.trim(),
            email = email.trim().lowercase(),
            passwordHash = hash,
            phoneNumber = phone.trim(),
            country = country.trim(),
            preferredCurrency = currency
        )
        val id = userDao.insertUser(user)
        val inserted = user.copy(id = id)

        // Seed default budget for new user
        budgetDao.insertBudget(
            BudgetEntity(
                userId = id,
                period = "DAILY",
                amount = 100.0,
                warningThresholdPercent = 80
            )
        )
        budgetDao.insertBudget(
            BudgetEntity(
                userId = id,
                period = "WEEKLY",
                amount = 600.0,
                warningThresholdPercent = 80
            )
        )
        budgetDao.insertBudget(
            BudgetEntity(
                userId = id,
                period = "MONTHLY",
                amount = 2500.0,
                warningThresholdPercent = 80
            )
        )

        Result.success(inserted)
    }

    suspend fun login(email: String, passwordPlain: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            val user = userDao.getUserByEmail(email.trim().lowercase())
                ?: return@withContext Result.failure(Exception("No account found with this email."))
            if (!SecurityHelper.verifyPassword(passwordPlain, user.passwordHash)) {
                return@withContext Result.failure(Exception("Incorrect password. Please try again."))
            }
            Result.success(user)
        }

    suspend fun resetPassword(email: String, newPasswordPlain: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val user = userDao.getUserByEmail(email.trim().lowercase())
                ?: return@withContext Result.failure(Exception("No account found with this email."))
            val (isStrong, msg) = SecurityHelper.isPasswordStrong(newPasswordPlain)
            if (!isStrong) {
                return@withContext Result.failure(Exception(msg))
            }
            val newHash = SecurityHelper.hashPassword(newPasswordPlain)
            userDao.updatePassword(user.id, newHash)
            Result.success(Unit)
        }

    fun observeUser(userId: Long): Flow<UserEntity?> = userDao.observeUserById(userId)

    suspend fun getUser(userId: Long): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun updateCurrency(userId: Long, currency: String) = withContext(Dispatchers.IO) {
        userDao.updateCurrency(userId, currency)
    }

    suspend fun updatePin(userId: Long, pin: String?) = withContext(Dispatchers.IO) {
        userDao.updatePin(userId, pin)
    }

    suspend fun deleteUserAccount(userId: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteAllForUser(userId)
        budgetDao.deleteAllForUser(userId)
        savingsGoalDao.deleteAllForUser(userId)
        recurringTransactionDao.deleteAllForUser(userId)
        billReminderDao.deleteAllForUser(userId)
        categoryDao.deleteAllCustomForUser(userId)
        userDao.deleteUserById(userId)
    }

    // --- Transactions ---
    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        withContext(Dispatchers.IO) {
            transactionDao.insertTransaction(transaction)
        }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long, userId: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(id, userId)
    }

    fun getAllTransactions(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions(userId)

    fun getRecentTransactions(userId: Long, limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(userId, limit)

    fun getTransactionsInRange(userId: Long, start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsInRange(userId, start, end)

    fun getTotalIncome(userId: Long): Flow<Double> =
        transactionDao.getTotalByType(userId, "INCOME")

    fun getTotalExpense(userId: Long): Flow<Double> =
        transactionDao.getTotalByType(userId, "EXPENSE")

    fun getSumByTypeInRange(
        userId: Long,
        type: String,
        start: Long,
        end: Long
    ): Flow<Double> = transactionDao.getSumByTypeInRange(userId, type, start, end)

    suspend fun getSumByTypeInRangeSync(
        userId: Long,
        type: String,
        start: Long,
        end: Long
    ): Double = withContext(Dispatchers.IO) {
        transactionDao.getSumByTypeInRangeSync(userId, type, start, end)
    }

    fun getCategorySpending(
        userId: Long,
        start: Long,
        end: Long
    ): Flow<List<CategorySum>> = transactionDao.getCategorySpending(userId, start, end)

    suspend fun getCategorySpendingSync(
        userId: Long,
        start: Long,
        end: Long
    ): List<CategorySum> = withContext(Dispatchers.IO) {
        transactionDao.getCategorySpendingSync(userId, start, end)
    }

    // --- Budgets ---
    fun getAllBudgets(userId: Long): Flow<List<BudgetEntity>> = budgetDao.getAllBudgets(userId)

    fun getOverallBudget(userId: Long, period: String): Flow<BudgetEntity?> =
        budgetDao.getOverallBudget(userId, period)

    suspend fun getOverallBudgetSync(userId: Long, period: String): BudgetEntity? =
        withContext(Dispatchers.IO) {
            budgetDao.getOverallBudgetSync(userId, period)
        }

    suspend fun saveBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(id: Long, userId: Long) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudget(id, userId)
    }

    // --- Savings Goals ---
    fun getAllGoals(userId: Long): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals(userId)

    suspend fun insertGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        savingsGoalDao.insertGoal(goal)
    }

    suspend fun addGoalContribution(id: Long, userId: Long, amount: Double) =
        withContext(Dispatchers.IO) {
            savingsGoalDao.addContribution(id, userId, amount)
        }

    suspend fun deleteGoal(id: Long, userId: Long) = withContext(Dispatchers.IO) {
        savingsGoalDao.deleteGoal(id, userId)
    }

    // --- Bill Reminders ---
    fun getAllBills(userId: Long): Flow<List<BillReminderEntity>> = billReminderDao.getAllBills(userId)

    suspend fun insertBill(bill: BillReminderEntity) = withContext(Dispatchers.IO) {
        billReminderDao.insertBill(bill)
    }

    suspend fun setBillPaidStatus(id: Long, userId: Long, isPaid: Boolean) =
        withContext(Dispatchers.IO) {
            billReminderDao.setBillPaidStatus(id, userId, isPaid)
        }

    suspend fun deleteBill(id: Long, userId: Long) = withContext(Dispatchers.IO) {
        billReminderDao.deleteBill(id, userId)
    }

    // --- Recurring Transactions ---
    fun getAllRecurring(userId: Long): Flow<List<RecurringTransactionEntity>> =
        recurringTransactionDao.getAll(userId)

    suspend fun insertRecurring(item: RecurringTransactionEntity) = withContext(Dispatchers.IO) {
        recurringTransactionDao.insert(item)
    }

    suspend fun deleteRecurring(id: Long, userId: Long) = withContext(Dispatchers.IO) {
        recurringTransactionDao.delete(id, userId)
    }

    // --- Categories ---
    fun getCategoriesByType(userId: Long, type: String): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(userId, type)

    fun getAllCategories(userId: Long): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories(userId)

    suspend fun addCustomCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category.copy(isCustom = true))
    }

    suspend fun deleteCustomCategory(id: Long, userId: Long) = withContext(Dispatchers.IO) {
        categoryDao.deleteCustomCategory(id, userId)
    }

    // --- Data Export ---
    suspend fun generateCsvExport(
        userId: Long,
        currencyCode: String
    ): String = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        val transactions = transactionDao.getAllTransactionsSync(userId)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val sb = StringBuilder()
        sb.appendLine("CEDIWISE FINANCIAL EXPORT")
        sb.appendLine("User Name,${user?.fullName ?: "User"}")
        sb.appendLine("Email,${user?.email ?: ""}")
        sb.appendLine("Export Date,${dateFormat.format(Date())}")
        sb.appendLine("Currency,$currencyCode")
        sb.appendLine()

        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        sb.appendLine("SUMMARY")
        sb.appendLine("Total Income,${CurrencyFormatter.format(totalIncome, currencyCode)}")
        sb.appendLine("Total Expenses,${CurrencyFormatter.format(totalExpense, currencyCode)}")
        sb.appendLine("Net Balance,${CurrencyFormatter.format(netBalance, currencyCode)}")
        sb.appendLine()

        sb.appendLine("TRANSACTIONS")
        sb.appendLine("Date,Time,Type,Category,Description,Amount,Payment Method,Note")
        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.dateEpochMillis))
            val cleanDesc = tx.description.replace(",", ";")
            val cleanNote = tx.note.replace(",", ";")
            sb.appendLine(
                "$dateStr,${tx.timeString},${tx.type},${tx.category},$cleanDesc,${tx.amount},${tx.paymentMethod},$cleanNote"
            )
        }
        sb.toString()
    }
}
