package com.example.ui.screens.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    mainViewModel: MainViewModel,
    initialIsExpense: Boolean = true,
    existingTransaction: TransactionEntity? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val user by mainViewModel.userProfile.collectAsState()
    val currency = user?.preferredCurrency ?: "GHS"
    val allCategories by mainViewModel.categories.collectAsState()

    var isExpense by remember {
        mutableStateOf(existingTransaction?.type?.let { it == "EXPENSE" } ?: initialIsExpense)
    }

    var amountText by remember {
        mutableStateOf(existingTransaction?.amount?.let { if (it > 0) it.toString() else "" } ?: "")
    }
    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }
    var selectedCategory by remember {
        mutableStateOf(
            existingTransaction?.category ?: if (isExpense) "Food & Dining" else "Salary & Wages"
        )
    }
    var selectedPaymentMethod by remember {
        mutableStateOf(existingTransaction?.paymentMethod ?: "Cash")
    }
    var note by remember { mutableStateOf(existingTransaction?.note ?: "") }
    var location by remember { mutableStateOf(existingTransaction?.location ?: "") }
    var isRecurring by remember { mutableStateOf(existingTransaction?.isRecurring ?: false) }

    var dateEpochMillis by remember {
        mutableLongStateOf(existingTransaction?.dateEpochMillis ?: System.currentTimeMillis())
    }
    var timeString by remember {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        mutableStateOf(existingTransaction?.timeString ?: sdf.format(Date()))
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddCustomCategoryDialog by remember { mutableStateOf(false) }
    var newCustomCategoryName by remember { mutableStateOf("") }

    val paymentMethods = listOf(
        "Cash",
        "MTN MoMo",
        "Telecel Cash",
        "AT Money",
        "Bank Transfer",
        "Debit / Credit Card",
        "Other"
    )

    // Filter categories by type
    val filteredCategories = allCategories.filter {
        it.type == if (isExpense) "EXPENSE" else "INCOME"
    }.ifEmpty {
        if (isExpense) listOf("Food & Dining", "Trotro / Transport", "Airtime & Data", "Rent", "Utilities")
            .map { com.example.data.local.entity.CategoryEntity(name = it, type = "EXPENSE", iconName = "", colorHex = "#EF4444") }
        else listOf("Salary & Wages", "Business / Sales", "Mobile Money Inflow", "Allowance")
            .map { com.example.data.local.entity.CategoryEntity(name = it, type = "INCOME", iconName = "", colorHex = "#10B981") }
    }

    val dateFormatter = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())

    // DatePicker
    val calendar = Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            dateEpochMillis = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // TimePicker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingTransaction != null) "Edit Transaction"
                        else if (isExpense) "Record Expense" else "Record Income"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_screen_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Segmented Switcher (Expense / Income)
            if (existingTransaction == null) {
                TabRow(
                    selectedTabIndex = if (isExpense) 0 else 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = isExpense,
                        onClick = {
                            isExpense = true
                            selectedCategory = "Food & Dining"
                        },
                        text = {
                            Text(
                                "Expense",
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("tab_expense")
                    )
                    Tab(
                        selected = !isExpense,
                        onClick = {
                            isExpense = false
                            selectedCategory = "Salary & Wages"
                        },
                        text = {
                            Text(
                                "Income",
                                fontWeight = FontWeight.Bold,
                                color = if (!isExpense) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("tab_income")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (errorMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(errorMessage!!)
                }
            }

            // Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpense) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Amount ($currency)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { char -> char.isDigit() || char == '.' }
                            errorMessage = null
                        },
                        placeholder = { Text("0.00", fontSize = 28.sp) },
                        prefix = {
                            Text(
                                text = "$currency ",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) ExpenseRed else IncomeGreen
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) ExpenseRed else IncomeGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    errorMessage = null
                },
                label = { Text("Description / What was this for? *") },
                placeholder = { Text(if (isExpense) "e.g. Lunch at Chop Bar, Trotro to Accra, MoMo to Mom" else "e.g. Monthly Salary, Freelance project") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection Header + Add Custom Category button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category *",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(
                    onClick = { showAddCustomCategoryDialog = true },
                    modifier = Modifier.testTag("add_custom_category_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Category")
                }
            }

            // Categories Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredCategories.forEach { cat ->
                    val isSelected = selectedCategory == cat.name
                    val icon = CategoryIconHelper.getIcon(cat.iconName, cat.name)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("category_chip_${cat.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Section
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paymentMethods.forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPaymentMethod = method },
                        label = { Text(method) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null,
                        modifier = Modifier.testTag("pay_method_$method")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date picker trigger
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormatter.format(Date(dateEpochMillis)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Time picker trigger
                Card(
                    modifier = Modifier
                        .weight(0.7f)
                        .clickable { timePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location (Optional)
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                placeholder = { Text("e.g. Makola Market, Circle, Kumasi, Home") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Note (Optional)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note / Remarks (Optional)") },
                placeholder = { Text("Additional details about this transaction") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recurring Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recurring Transaction",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Repeat this transaction automatically (Rent, Subscriptions)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    modifier = Modifier.testTag("recurring_switch")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Update Button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@Button
                    }
                    if (description.isBlank()) {
                        errorMessage = "Please enter a description"
                        return@Button
                    }

                    if (existingTransaction != null) {
                        val updated = existingTransaction.copy(
                            type = if (isExpense) "EXPENSE" else "INCOME",
                            amount = amount,
                            category = selectedCategory,
                            description = description.trim(),
                            paymentMethod = selectedPaymentMethod,
                            dateEpochMillis = dateEpochMillis,
                            timeString = timeString,
                            note = note.trim(),
                            location = location.trim().ifEmpty { null },
                            isRecurring = isRecurring
                        )
                        mainViewModel.updateTransaction(updated)
                    } else {
                        mainViewModel.addTransaction(
                            type = if (isExpense) "EXPENSE" else "INCOME",
                            amount = amount,
                            category = selectedCategory,
                            description = description.trim(),
                            paymentMethod = selectedPaymentMethod,
                            dateEpochMillis = dateEpochMillis,
                            timeString = timeString,
                            note = note.trim(),
                            isRecurring = isRecurring,
                            context = context
                        )
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) ExpenseRed else IncomeGreen
                )
            ) {
                Text(
                    text = if (existingTransaction != null) "Update Transaction" else "Save Transaction",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Custom Category Dialog
    if (showAddCustomCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomCategoryDialog = false },
            title = { Text("Create Custom Category") },
            text = {
                Column {
                    Text("Add a personalized category to your list:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCustomCategoryName,
                        onValueChange = { newCustomCategoryName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomCategoryName.isNotBlank()) {
                            mainViewModel.addCustomCategory(
                                name = newCustomCategoryName.trim(),
                                type = if (isExpense) "EXPENSE" else "INCOME",
                                iconName = "Category",
                                colorHex = if (isExpense) "#EF4444" else "#10B981"
                            )
                            selectedCategory = newCustomCategoryName.trim()
                            newCustomCategoryName = ""
                            showAddCustomCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
