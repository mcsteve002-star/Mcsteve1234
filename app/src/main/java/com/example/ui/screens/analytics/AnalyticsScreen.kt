package com.example.ui.screens.analytics

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BillReminderEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.ui.components.CategoryIconHelper
import com.example.ui.screens.home.BudgetMeterCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(mainViewModel: MainViewModel) {
    val user by mainViewModel.userProfile.collectAsState()
    val currency = user?.preferredCurrency ?: "GHS"

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Analytics", "Budgets", "Savings", "Bills")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_$title")
                )
            }
        }

        when (selectedTab) {
            0 -> AnalyticsTab(mainViewModel, currency)
            1 -> BudgetsTab(mainViewModel, currency)
            2 -> SavingsTab(mainViewModel, currency)
            3 -> BillsTab(mainViewModel, currency)
        }
    }
}

@Composable
fun AnalyticsTab(mainViewModel: MainViewModel, currency: String) {
    val totalIncome by mainViewModel.totalIncome.collectAsState()
    val totalExpense by mainViewModel.totalExpense.collectAsState()
    val categorySpending by mainViewModel.categorySpendingList.collectAsState()
    val insights by mainViewModel.insights.collectAsState()

    var selectedPeriod by remember { mutableStateOf("30 Days") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7 Days", "30 Days", "3 Months", "12 Months").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period) }
                    )
                }
            }
        }

        // Income vs Expense comparison card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cashflow Comparison",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val totalFlow = (totalIncome + totalExpense).coerceAtLeast(1.0)
                    val incomeRatio = (totalIncome / totalFlow).toFloat()
                    val expenseRatio = (totalExpense / totalFlow).toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(incomeRatio.coerceAtLeast(0.01f))
                                .background(IncomeGreen)
                        )
                        Box(
                            modifier = Modifier
                                .weight(expenseRatio.coerceAtLeast(0.01f))
                                .background(ExpenseRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Income: ${CurrencyFormatter.format(totalIncome, currency)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Expenses: ${CurrencyFormatter.format(totalExpense, currency)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Financial Insights Feed
        item {
            Text(
                text = "Financial Insights",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(insights) { insight ->
            val icon = when (insight.type) {
                "WARNING" -> Icons.Default.Warning
                "SUCCESS" -> Icons.Default.CheckCircle
                else -> Icons.Default.Lightbulb
            }
            val color = when (insight.type) {
                "WARNING" -> WarningOrange
                "SUCCESS" -> IncomeGreen
                else -> TealPrimary
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = insight.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Spending By Category
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (categorySpending.isEmpty()) {
            item {
                Text(
                    text = "No category spending data for this period yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val totalCatSpending = categorySpending.sumOf { it.total }.coerceAtLeast(1.0)
            items(categorySpending) { cat ->
                val pct = (cat.total / totalCatSpending * 100.0)
                val icon = CategoryIconHelper.getIcon("", cat.category)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = cat.category,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "${cat.count} transaction(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.format(cat.total, currency),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = CurrencyFormatter.formatPercentage(pct),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetsTab(mainViewModel: MainViewModel, currency: String) {
    val dailyBudget by mainViewModel.dailyBudgetStatus.collectAsState()
    val weeklyBudget by mainViewModel.weeklyBudgetStatus.collectAsState()
    val monthlyBudget by mainViewModel.monthlyBudgetStatus.collectAsState()

    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var editingPeriod by remember { mutableStateOf("MONTHLY") }
    var editingAmount by remember { mutableStateOf("") }
    var editingThreshold by remember { mutableIntStateOf(80) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Budget Limits & Monitoring",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Set limits to receive automatic early warnings when approaching limits.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Daily
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Budget",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = {
                            editingPeriod = "DAILY"
                            editingAmount = dailyBudget?.limit?.toString() ?: "100"
                            editingThreshold = 80
                            showEditBudgetDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Daily Budget")
                        }
                    }
                    dailyBudget?.let { BudgetMeterCard(it, "Today's Limit", currency) }
                }
            }
        }

        // Weekly
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Budget",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = {
                            editingPeriod = "WEEKLY"
                            editingAmount = weeklyBudget?.limit?.toString() ?: "600"
                            editingThreshold = 80
                            showEditBudgetDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Weekly Budget")
                        }
                    }
                    weeklyBudget?.let { BudgetMeterCard(it, "Weekly Limit", currency) }
                }
            }
        }

        // Monthly
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = {
                            editingPeriod = "MONTHLY"
                            editingAmount = monthlyBudget?.limit?.toString() ?: "2500"
                            editingThreshold = 80
                            showEditBudgetDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Monthly Budget")
                        }
                    }
                    monthlyBudget?.let { BudgetMeterCard(it, "Monthly Limit", currency) }
                }
            }
        }
    }

    if (showEditBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showEditBudgetDialog = false },
            title = { Text("Set $editingPeriod Budget") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editingAmount,
                        onValueChange = { editingAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Budget Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Alert Warning Threshold: $editingThreshold%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = editingThreshold.toFloat(),
                        onValueChange = { editingThreshold = it.toInt() },
                        valueRange = 50f..95f,
                        steps = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = editingAmount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            mainViewModel.saveBudget(editingPeriod, amt, editingThreshold)
                            showEditBudgetDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBudgetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SavingsTab(mainViewModel: MainViewModel, currency: String) {
    val context = LocalContext.current
    val goals by mainViewModel.savingsGoals.collectAsState()

    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var goalToContribute by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var contributionAmount by remember { mutableStateOf("") }

    var newGoalTitle by remember { mutableStateOf("") }
    var newGoalTarget by remember { mutableStateOf("") }
    var newGoalDateMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val cal = Calendar.getInstance().apply { set(y, m, d) }
            newGoalDateMillis = cal.timeInMillis
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Savings Goals",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Track progress toward your aspirations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateGoalDialog = true },
                    modifier = Modifier.testTag("create_goal_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Goal")
                }
            }
        }

        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Savings Goals yet", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Set a target for an emergency fund, new car, laptop, or land advance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                val progress = if (goal.targetAmount > 0) (goal.currentSaved / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                val remaining = (goal.targetAmount - goal.currentSaved).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(onClick = { mainViewModel.deleteSavingsGoal(goal.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = IncomeGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Saved: ${CurrencyFormatter.format(goal.currentSaved, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Target: ${CurrencyFormatter.format(goal.targetAmount, currency)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "Target Date: ${dateFormat.format(Date(goal.targetDateEpochMillis))} (${CurrencyFormatter.format(remaining, currency)} remaining)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                goalToContribute = goal
                                contributionAmount = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text("+ Add Contribution", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Create Goal Dialog
    if (showCreateGoalDialog) {
        AlertDialog(
            onDismissRequest = { showCreateGoalDialog = false },
            title = { Text("Create Savings Goal") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        label = { Text("Goal Title") },
                        placeholder = { Text("e.g. New Laptop, Rent Advance") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newGoalTarget,
                        onValueChange = { newGoalTarget = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Target Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dateFormat.format(Date(newGoalDateMillis)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Date") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = newGoalTarget.toDoubleOrNull()
                        if (newGoalTitle.isNotBlank() && target != null && target > 0) {
                            mainViewModel.addSavingsGoal(
                                title = newGoalTitle.trim(),
                                targetAmount = target,
                                targetDateMillis = newGoalDateMillis,
                                note = "",
                                colorHex = "#00897B"
                            )
                            newGoalTitle = ""
                            newGoalTarget = ""
                            showCreateGoalDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Contribute Dialog
    if (goalToContribute != null) {
        val goal = goalToContribute!!
        AlertDialog(
            onDismissRequest = { goalToContribute = null },
            title = { Text("Contribute to ${goal.title}") },
            text = {
                Column {
                    Text("Enter the amount you're depositing into this savings goal:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contributionAmount,
                        onValueChange = { contributionAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = contributionAmount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            mainViewModel.contributeToGoal(goal.id, amt)
                            goalToContribute = null
                        }
                    }
                ) {
                    Text("Deposit")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToContribute = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BillsTab(mainViewModel: MainViewModel, currency: String) {
    val bills by mainViewModel.billReminders.collectAsState()
    val context = LocalContext.current

    var showCreateBillDialog by remember { mutableStateOf(false) }
    var billTitle by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }
    var billCategory by remember { mutableStateOf("Utilities") }
    var billDueDateMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val cal = Calendar.getInstance().apply { set(y, m, d) }
            billDueDateMillis = cal.timeInMillis
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Upcoming Bills",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Never miss internet, electricity, or school fees",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateBillDialog = true },
                    modifier = Modifier.testTag("add_bill_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Bill")
                }
            }
        }

        if (bills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Bill Reminders", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Add reminders for ECG, Ghana Water, DSTV, or rent to get notified automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(bills, key = { it.id }) { bill ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (bill.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { mainViewModel.toggleBillPaid(bill) }) {
                                Icon(
                                    imageVector = if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (bill.isPaid) "Paid" else "Unpaid",
                                    tint = if (bill.isPaid) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = bill.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Due: ${dateFormat.format(Date(bill.dueDateEpochMillis))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (bill.dueDateEpochMillis < System.currentTimeMillis() && !bill.isPaid) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = CurrencyFormatter.format(bill.amount, currency),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else ExpenseRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { mainViewModel.deleteBill(bill.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateBillDialog) {
        AlertDialog(
            onDismissRequest = { showCreateBillDialog = false },
            title = { Text("Add Bill Reminder") },
            text = {
                Column {
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("Bill Name") },
                        placeholder = { Text("e.g. ECG Electricity, MTN Fibre, DSTV") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dateFormat.format(Date(billDueDateMillis)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Date") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = billAmount.toDoubleOrNull()
                        if (billTitle.isNotBlank() && amt != null && amt > 0) {
                            mainViewModel.addBill(
                                title = billTitle.trim(),
                                amount = amt,
                                category = billCategory,
                                dueDateMillis = billDueDateMillis,
                                recurrence = "MONTHLY"
                            )
                            billTitle = ""
                            billAmount = ""
                            showCreateBillDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateBillDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
