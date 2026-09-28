package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.CategorySum
import com.example.data.local.entity.BillReminderEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.data.preferences.PreferenceManager
import com.example.data.repository.CediWiseRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class BudgetStatus(
    val period: String, // "DAILY", "WEEKLY", "MONTHLY"
    val limit: Double,
    val spent: Double,
    val remaining: Double,
    val percentageUsed: Double,
    val isApproaching: Boolean,
    val isExceeded: Boolean
)

data class FinancialInsight(
    val title: String,
    val description: String,
    val type: String // "INFO", "WARNING", "SUCCESS"
)

data class TransactionFilter(
    val query: String = "",
    val type: String = "ALL", // "ALL", "EXPENSE", "INCOME"
    val category: String = "ALL",
    val paymentMethod: String = "ALL",
    val sortOption: String = "NEWEST" // "NEWEST", "OLDEST", "AMOUNT_HIGH", "AMOUNT_LOW"
)

class MainViewModel(
    private val repository: CediWiseRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val currentUserId: Long get() = preferenceManager.getCurrentUserId()

    // User profile
    private val _userProfile = MutableStateFlow<UserEntity?>(null)
    val userProfile: StateFlow<UserEntity?> = _userProfile.asStateFlow()

    // Transactions
    private val _rawTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val rawTransactions: StateFlow<List<TransactionEntity>> = _rawTransactions.asStateFlow()

    // Filters and Search
    val filterState = MutableStateFlow(TransactionFilter())
    val searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    val filterType: MutableStateFlow<String> = MutableStateFlow("ALL")
    val filterCategory: MutableStateFlow<String> = MutableStateFlow("ALL")
    val filterPaymentMethod: MutableStateFlow<String> = MutableStateFlow("ALL")
    val sortOption: MutableStateFlow<String> = MutableStateFlow("NEWEST")

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        _rawTransactions,
        searchQuery,
        filterType,
        sortOption
    ) { list, query, type, sort ->
        var result = list

        if (type != "ALL") {
            result = result.filter { it.type == type }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.description.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.note.lowercase().contains(q) ||
                        it.amount.toString().contains(q) ||
                        it.paymentMethod.lowercase().contains(q)
            }
        }

        when (sort) {
            "NEWEST" -> result.sortedByDescending { it.dateEpochMillis }
            "OLDEST" -> result.sortedBy { it.dateEpochMillis }
            "AMOUNT_HIGH" -> result.sortedByDescending { it.amount }
            "AMOUNT_LOW" -> result.sortedBy { it.amount }
            else -> result.sortedByDescending { it.dateEpochMillis }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Balances
    val totalIncome = MutableStateFlow(0.0)
    val totalExpense = MutableStateFlow(0.0)
    val netBalance = MutableStateFlow(0.0)

    val todaySpending = MutableStateFlow(0.0)
    val weekSpending = MutableStateFlow(0.0)
    val monthSpending = MutableStateFlow(0.0)

    // Budgets
    val dailyBudgetStatus = MutableStateFlow<BudgetStatus?>(null)
    val weeklyBudgetStatus = MutableStateFlow<BudgetStatus?>(null)
    val monthlyBudgetStatus = MutableStateFlow<BudgetStatus?>(null)

    // Analytics Category breakdown
    val categorySpendingList = MutableStateFlow<List<CategorySum>>(emptyList())
    val insights = MutableStateFlow<List<FinancialInsight>>(emptyList())

    // Savings Goals & Bills & Recurring
    val savingsGoals = MutableStateFlow<List<SavingsGoalEntity>>(emptyList())
    val billReminders = MutableStateFlow<List<BillReminderEntity>>(emptyList())
    val recurringTransactions = MutableStateFlow<List<RecurringTransactionEntity>>(emptyList())
    val categories = MutableStateFlow<List<CategoryEntity>>(emptyList())

    init {
        loadUserData()
    }

    fun loadUserData() {
        val uid = currentUserId
        if (uid <= 0) return

        viewModelScope.launch {
            _userProfile.value = repository.getUser(uid)

            // Collect transactions
            repository.getAllTransactions(uid).collect { list ->
                _rawTransactions.value = list
                calculateSummaries(list)
                calculateBudgets()
                generateInsights(list)
            }
        }

        viewModelScope.launch {
            repository.getAllGoals(uid).collect {
                savingsGoals.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllBills(uid).collect {
                billReminders.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllRecurring(uid).collect {
                recurringTransactions.value = it
            }
        }

        viewModelScope.launch {
            repository.getAllCategories(uid).collect {
                categories.value = it
            }
        }
    }

    private fun calculateSummaries(list: List<TransactionEntity>) {
        val income = list.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        totalIncome.value = income
        totalExpense.value = expense
        netBalance.value = income - expense

        val (todayStart, todayEnd) = getTodayRange()
        val (weekStart, weekEnd) = getWeekRange()
        val (monthStart, monthEnd) = getMonthRange()

        todaySpending.value = list.filter {
            it.type == "EXPENSE" && it.dateEpochMillis in todayStart..todayEnd
        }.sumOf { it.amount }

        weekSpending.value = list.filter {
            it.type == "EXPENSE" && it.dateEpochMillis in weekStart..weekEnd
        }.sumOf { it.amount }

        monthSpending.value = list.filter {
            it.type == "EXPENSE" && it.dateEpochMillis in monthStart..monthEnd
        }.sumOf { it.amount }
    }

    private suspend fun calculateBudgets() {
        val uid = currentUserId
        if (uid <= 0) return

        val dailyBudget = repository.getOverallBudgetSync(uid, "DAILY")
        val weeklyBudget = repository.getOverallBudgetSync(uid, "WEEKLY")
        val monthlyBudget = repository.getOverallBudgetSync(uid, "MONTHLY")

        dailyBudget?.let { b ->
            val spent = todaySpending.value
            val remaining = (b.amount - spent).coerceAtLeast(0.0)
            val pct = if (b.amount > 0) (spent / b.amount) * 100.0 else 0.0
            dailyBudgetStatus.value = BudgetStatus(
                period = "DAILY",
                limit = b.amount,
                spent = spent,
                remaining = remaining,
                percentageUsed = pct,
                isApproaching = pct >= b.warningThresholdPercent && pct < 100.0,
                isExceeded = spent > b.amount
            )
        }

        weeklyBudget?.let { b ->
            val spent = weekSpending.value
            val remaining = (b.amount - spent).coerceAtLeast(0.0)
            val pct = if (b.amount > 0) (spent / b.amount) * 100.0 else 0.0
            weeklyBudgetStatus.value = BudgetStatus(
                period = "WEEKLY",
                limit = b.amount,
                spent = spent,
                remaining = remaining,
                percentageUsed = pct,
                isApproaching = pct >= b.warningThresholdPercent && pct < 100.0,
                isExceeded = spent > b.amount
            )
        }

        monthlyBudget?.let { b ->
            val spent = monthSpending.value
            val remaining = (b.amount - spent).coerceAtLeast(0.0)
            val pct = if (b.amount > 0) (spent / b.amount) * 100.0 else 0.0
            monthlyBudgetStatus.value = BudgetStatus(
                period = "MONTHLY",
                limit = b.amount,
                spent = spent,
                remaining = remaining,
                percentageUsed = pct,
                isApproaching = pct >= b.warningThresholdPercent && pct < 100.0,
                isExceeded = spent > b.amount
            )
        }

        val (monthStart, monthEnd) = getMonthRange()
        categorySpendingList.value = repository.getCategorySpendingSync(uid, monthStart, monthEnd)
    }

    private fun generateInsights(list: List<TransactionEntity>) {
        val result = mutableListOf<FinancialInsight>()
        val currency = _userProfile.value?.preferredCurrency ?: "GHS"

        val (todayStart, todayEnd) = getTodayRange()
        val expensesToday = list.filter { it.type == "EXPENSE" && it.dateEpochMillis in todayStart..todayEnd }

        if (expensesToday.isEmpty()) {
            result.add(
                FinancialInsight(
                    title = "No expenses recorded today",
                    description = "Take a moment to record your morning commute, MoMo transfers, or meals.",
                    type = "INFO"
                )
            )
        } else {
            val largestToday = expensesToday.maxByOrNull { it.amount }
            if (largestToday != null) {
                result.add(
                    FinancialInsight(
                        title = "Largest expense today",
                        description = "${largestToday.category}: ${CurrencyFormatter.format(largestToday.amount, currency)} (${largestToday.description})",
                        type = "INFO"
                    )
                )
            }
        }

        // Monthly budget alert
        monthlyBudgetStatus.value?.let { mb ->
            if (mb.isExceeded) {
                val over = mb.spent - mb.limit
                result.add(
                    FinancialInsight(
                        title = "Monthly Budget Exceeded!",
                        description = "You have exceeded your monthly limit by ${CurrencyFormatter.format(over, currency)}.",
                        type = "WARNING"
                    )
                )
            } else if (mb.isApproaching) {
                result.add(
                    FinancialInsight(
                        title = "Approaching Monthly Budget",
                        description = "You have used ${CurrencyFormatter.formatPercentage(mb.percentageUsed)} of your budget. ${CurrencyFormatter.format(mb.remaining, currency)} remaining.",
                        type = "WARNING"
                    )
                )
            }
        }

        // Top category insight
        val topCat = categorySpendingList.value.firstOrNull()
        if (topCat != null && totalExpense.value > 0) {
            val pct = (topCat.total / totalExpense.value) * 100.0
            result.add(
                FinancialInsight(
                    title = "${topCat.category} is your top expense",
                    description = "Accounting for ${CurrencyFormatter.formatPercentage(pct)} of your total recorded expenses (${CurrencyFormatter.format(topCat.total, currency)}).",
                    type = "INFO"
                )
            )
        }

        // Daily average insight
        val daysInMonthPassed = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (daysInMonthPassed > 0) {
            val dailyAvg = monthSpending.value / daysInMonthPassed
            result.add(
                FinancialInsight(
                    title = "Average daily spend",
                    description = "Your average daily spending this month is ${CurrencyFormatter.format(dailyAvg, currency)}.",
                    type = "SUCCESS"
                )
            )
        }

        insights.value = result
    }

    // --- Action Methods ---
    fun addTransaction(
        type: String,
        amount: Double,
        category: String,
        description: String,
        paymentMethod: String,
        dateEpochMillis: Long,
        timeString: String,
        note: String,
        isRecurring: Boolean,
        context: Context? = null
    ) {
        val uid = currentUserId
        if (uid <= 0 || amount <= 0) return

        viewModelScope.launch {
            val entity = TransactionEntity(
                userId = uid,
                type = type,
                amount = amount,
                category = category,
                description = description,
                paymentMethod = paymentMethod,
                dateEpochMillis = dateEpochMillis,
                timeString = timeString,
                note = note,
                isRecurring = isRecurring
            )
            repository.insertTransaction(entity)

            // Budget alert check if expense
            if (type == "EXPENSE" && context != null) {
                val budget = repository.getOverallBudgetSync(uid, "DAILY")
                if (budget != null) {
                    val newSpent = todaySpending.value + amount
                    if (newSpent > budget.amount) {
                        NotificationHelper.showNotification(
                            context = context,
                            channelId = NotificationHelper.CHANNEL_BUDGETS,
                            notificationId = 302,
                            title = "Daily Budget Warning ⚠️",
                            message = "Your daily budget of ${CurrencyFormatter.format(budget.amount, _userProfile.value?.preferredCurrency ?: "GHS")} has been exceeded!"
                        )
                    }
                }
            }
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx.id, currentUserId)
        }
    }

    fun saveBudget(period: String, amount: Double, threshold: Int) {
        val uid = currentUserId
        if (uid <= 0) return

        viewModelScope.launch {
            val existing = repository.getOverallBudgetSync(uid, period)
            val budget = existing?.copy(amount = amount, warningThresholdPercent = threshold)
                ?: BudgetEntity(
                    userId = uid,
                    period = period,
                    amount = amount,
                    warningThresholdPercent = threshold
                )
            repository.saveBudget(budget)
            calculateBudgets()
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double, targetDateMillis: Long, note: String, colorHex: String) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoalEntity(
                    userId = uid,
                    title = title,
                    targetAmount = targetAmount,
                    targetDateEpochMillis = targetDateMillis,
                    note = note,
                    colorHex = colorHex
                )
            )
        }
    }

    fun contributeToGoal(goalId: Long, amount: Double) {
        val uid = currentUserId
        if (uid <= 0 || amount <= 0) return
        viewModelScope.launch {
            repository.addGoalContribution(goalId, uid, amount)
        }
    }

    fun deleteSavingsGoal(goalId: Long) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            repository.deleteGoal(goalId, uid)
        }
    }

    fun addBill(title: String, amount: Double, category: String, dueDateMillis: Long, recurrence: String) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            repository.insertBill(
                BillReminderEntity(
                    userId = uid,
                    title = title,
                    amount = amount,
                    category = category,
                    dueDateEpochMillis = dueDateMillis,
                    recurrence = recurrence
                )
            )
        }
    }

    fun toggleBillPaid(bill: BillReminderEntity) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            repository.setBillPaidStatus(bill.id, uid, !bill.isPaid)
        }
    }

    fun deleteBill(billId: Long) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            repository.deleteBill(billId, uid)
        }
    }

    fun addCustomCategory(name: String, type: String, iconName: String, colorHex: String) {
        val uid = currentUserId
        if (uid <= 0 || name.isBlank()) return
        viewModelScope.launch {
            repository.addCustomCategory(
                CategoryEntity(
                    userId = uid,
                    name = name.trim(),
                    type = type,
                    iconName = iconName,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateCurrency(currencyCode: String) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            repository.updateCurrency(uid, currencyCode)
            _userProfile.value = repository.getUser(uid)
            generateInsights(_rawTransactions.value)
        }
    }

    fun updatePin(newPin: String?) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            repository.updatePin(uid, newPin)
            _userProfile.value = repository.getUser(uid)
        }
    }

    fun updateReminderTimes(morningHour: Int, morningMin: Int, eveningHour: Int, eveningMin: Int, context: Context) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            val user = repository.getUser(uid) ?: return@launch
            val updated = user.copy(
                morningReminderHour = morningHour,
                morningReminderMinute = morningMin,
                eveningReminderHour = eveningHour,
                eveningReminderMinute = eveningMin
            )
            repository.updateUser(updated)
            _userProfile.value = updated
            AlarmScheduler.scheduleDailyReminders(context, morningHour, morningMin, eveningHour, eveningMin)
        }
    }

    fun sendTestNotification(context: Context) {
        NotificationHelper.showNotification(
            context = context,
            channelId = NotificationHelper.CHANNEL_REMINDERS,
            notificationId = 999,
            title = "CediWise Test Notification 🔔",
            message = "Reminders are actively scheduled and functional on your device!"
        )
    }

    suspend fun exportCsv(): String {
        val uid = currentUserId
        val curr = _userProfile.value?.preferredCurrency ?: "GHS"
        return repository.generateCsvExport(uid, curr)
    }

    fun deleteAccount(onComplete: () -> Unit) {
        val uid = currentUserId
        if (uid <= 0) return
        viewModelScope.launch {
            repository.deleteUserAccount(uid)
            preferenceManager.logout()
            onComplete()
        }
    }

    // --- Date Helper Utilities ---
    private fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis - 1
        return Pair(start, end)
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_WEEK, 7)
        val end = cal.timeInMillis - 1
        return Pair(start, end)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        return Pair(start, end)
    }
}
