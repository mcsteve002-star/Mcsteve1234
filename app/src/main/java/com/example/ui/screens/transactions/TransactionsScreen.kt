package com.example.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    mainViewModel: MainViewModel,
    onEditTransaction: (TransactionEntity) -> Unit
) {
    val user by mainViewModel.userProfile.collectAsState()
    val currency = user?.preferredCurrency ?: "GHS"

    val transactions by mainViewModel.filteredTransactions.collectAsState()
    val searchQuery by mainViewModel.searchQuery.collectAsState()
    val filterType by mainViewModel.filterType.collectAsState()
    val sortOption by mainViewModel.sortOption.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedTransactionForDetails by remember { mutableStateOf<TransactionEntity?>(null) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_screen")
    ) {
        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { mainViewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_input"),
                placeholder = { Text("Search description, category, MoMo, notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { mainViewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    } else {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Newest first") },
                    onClick = {
                        mainViewModel.sortOption.value = "NEWEST"
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Oldest first") },
                    onClick = {
                        mainViewModel.sortOption.value = "OLDEST"
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Highest amount") },
                    onClick = {
                        mainViewModel.sortOption.value = "AMOUNT_HIGH"
                        showSortMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lowest amount") },
                    onClick = {
                        mainViewModel.sortOption.value = "AMOUNT_LOW"
                        showSortMenu = false
                    }
                )
            }
        }

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == "ALL",
                onClick = { mainViewModel.filterType.value = "ALL" },
                label = { Text("All") },
                modifier = Modifier.testTag("filter_all")
            )
            FilterChip(
                selected = filterType == "EXPENSE",
                onClick = { mainViewModel.filterType.value = "EXPENSE" },
                label = { Text("Expenses Only") },
                modifier = Modifier.testTag("filter_expense")
            )
            FilterChip(
                selected = filterType == "INCOME",
                onClick = { mainViewModel.filterType.value = "INCOME" },
                label = { Text("Income Only") },
                modifier = Modifier.testTag("filter_income")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Transactions list
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No matching transactions found" else "No transactions in this filter",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("transactions_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    val isExpense = tx.type == "EXPENSE"
                    val icon = CategoryIconHelper.getIcon("", tx.category)
                    val color = CategoryIconHelper.getColor(if (isExpense) "#EF4444" else "#10B981")
                    val dateFormatted = dateFormat.format(Date(tx.dateEpochMillis))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTransactionForDetails = tx }
                            .testTag("transaction_row_${tx.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = tx.description.ifBlank { tx.category },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${tx.category} • $dateFormatted",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (tx.paymentMethod.isNotBlank()) {
                                        Text(
                                            text = tx.paymentMethod,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isExpense) "-" else "+"}${CurrencyFormatter.format(tx.amount, currency)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isExpense) ExpenseRed else IncomeGreen
                                )

                                Row {
                                    IconButton(
                                        onClick = { onEditTransaction(tx) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { transactionToDelete = tx },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(16.dp),
                                            tint = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = {
                Text("Are you sure you want to permanently delete this ${tx.type.lowercase()} of ${CurrencyFormatter.format(tx.amount, currency)}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Transaction Details Dialog
    if (selectedTransactionForDetails != null) {
        val tx = selectedTransactionForDetails!!
        AlertDialog(
            onDismissRequest = { selectedTransactionForDetails = null },
            title = { Text(tx.description.ifBlank { tx.category }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Amount: ${CurrencyFormatter.format(tx.amount, currency)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (tx.type == "EXPENSE") ExpenseRed else IncomeGreen
                    )
                    Text("Type: ${tx.type}")
                    Text("Category: ${tx.category}")
                    Text("Payment Method: ${tx.paymentMethod}")
                    Text("Date & Time: ${dateFormat.format(Date(tx.dateEpochMillis))} at ${tx.timeString}")
                    if (tx.note.isNotBlank()) {
                        Text("Note: ${tx.note}")
                    }
                    if (tx.isRecurring) {
                        Text("Recurring: Yes")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTransactionForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}
