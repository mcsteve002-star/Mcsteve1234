package com.example.ui.screens.settings

import android.app.TimePickerDialog
import android.content.Intent
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currencies
import com.example.data.preferences.PreferenceManager
import com.example.ui.theme.ExpenseRed
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    preferenceManager: PreferenceManager,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val user by mainViewModel.userProfile.collectAsState()
    val currency = user?.preferredCurrency ?: "GHS"
    val themeMode by preferenceManager.themeMode.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    var morningHour by remember { mutableIntStateOf(user?.morningReminderHour ?: 8) }
    var morningMinute by remember { mutableIntStateOf(user?.morningReminderMinute ?: 0) }
    var eveningHour by remember { mutableIntStateOf(user?.eveningReminderHour ?: 20) }
    var eveningMinute by remember { mutableIntStateOf(user?.eveningReminderMinute ?: 0) }

    val morningPicker = TimePickerDialog(
        context,
        { _, h, m ->
            morningHour = h
            morningMinute = m
            mainViewModel.updateReminderTimes(morningHour, morningMinute, eveningHour, eveningMinute, context)
        },
        morningHour,
        morningMinute,
        true
    )

    val eveningPicker = TimePickerDialog(
        context,
        { _, h, m ->
            eveningHour = h
            eveningMinute = m
            mainViewModel.updateReminderTimes(morningHour, morningMinute, eveningHour, eveningMinute, context)
        },
        eveningHour,
        eveningMinute,
        true
    )

    fun shareCsv() {
        CoroutineScope(Dispatchers.IO).launch {
            val csv = mainViewModel.exportCsv()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "CediWise Financial Export")
                putExtra(Intent.EXTRA_TEXT, csv)
            }
            context.startActivity(Intent.createChooser(intent, "Share CediWise Report"))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.fullName?.firstOrNull()?.uppercase() ?: "U"),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user?.fullName ?: "CediWise User",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = user?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${user?.country ?: "Ghana"} • $currency",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Section: Preferences
        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    // Currency
                    val curr = Currencies.fromCode(currency)
                    SettingsRow(
                        icon = Icons.Default.Paid,
                        title = "Primary Currency",
                        subtitle = "${curr.flag} ${curr.code} (${curr.symbol}) - ${curr.name}",
                        onClick = { showCurrencyDialog = true }
                    )

                    // Theme
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = "Theme Mode",
                        subtitle = when (themeMode) {
                            "DARK" -> "Dark mode"
                            "LIGHT" -> "Light mode"
                            else -> "System Default"
                        },
                        onClick = {
                            val nextMode = when (themeMode) {
                                "SYSTEM" -> "DARK"
                                "DARK" -> "LIGHT"
                                else -> "SYSTEM"
                            }
                            preferenceManager.setThemeMode(nextMode)
                        }
                    )
                }
            }
        }

        // Section: Reminders & Notifications
        item {
            Text(
                text = "Notifications & Reminders",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Schedule,
                        title = "Morning Reminder Time",
                        subtitle = String.format(Locale.getDefault(), "%02d:%02d", morningHour, morningMinute),
                        onClick = { morningPicker.show() }
                    )
                    SettingsRow(
                        icon = Icons.Default.Schedule,
                        title = "Evening Reminder Time",
                        subtitle = String.format(Locale.getDefault(), "%02d:%02d", eveningHour, eveningMinute),
                        onClick = { eveningPicker.show() }
                    )
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        title = "Send Test Notification",
                        subtitle = "Verify device notification scheduling",
                        onClick = { mainViewModel.sendTestNotification(context) }
                    )
                }
            }
        }

        // Section: Security
        item {
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Lock,
                        title = "App Lock PIN",
                        subtitle = if (user?.appLockPin.isNullOrBlank()) "No PIN configured (Tap to set)" else "PIN is active (Tap to change)",
                        onClick = { showPinDialog = true }
                    )
                }
            }
        }

        // Section: Data & Reports
        item {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Download,
                        title = "Export Financial Records (CSV)",
                        subtitle = "Share or download your full transaction and budget ledger",
                        onClick = { shareCsv() }
                    )
                }
            }
        }

        // Section: Legal & Account
        item {
            Text(
                text = "Legal & Support",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Help Center & FAQs",
                        subtitle = "Guide on tracking, MoMo, and budget alerts",
                        onClick = { showHelpDialog = true }
                    )
                    SettingsRow(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "Read our on-device privacy & data deletion guarantee",
                        onClick = { showPrivacyDialog = true }
                    )
                    SettingsRow(
                        icon = Icons.Default.Description,
                        title = "Terms of Service",
                        subtitle = "Review terms and conditions of usage",
                        onClick = { showTermsDialog = true }
                    )
                    SettingsRow(
                        icon = Icons.Default.Security,
                        title = "About CediWise",
                        subtitle = "Version 1.0.0 (Production Build)",
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }

        // Logout & Delete Buttons
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_account_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Permanently Delete Account & Financial Data", color = ExpenseRed)
            }
        }
    }

    // Currency Picker Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Primary Currency") },
            text = {
                Column {
                    Currencies.all.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mainViewModel.updateCurrency(c.code)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(c.flag, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${c.code} (${c.symbol}) - ${c.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (c.code == currency) FontWeight.Bold else FontWeight.Normal,
                                    color = if (c.code == currency) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    // App PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configure 4-Digit App PIN") },
            text = {
                Column {
                    Text(
                        "Set a 4-digit numeric PIN to protect your personal financial records upon opening the app. Leave blank to disable.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { if (it.length <= 4) newPinText = it.filter { c -> c.isDigit() } },
                        label = { Text("4-digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pin = if (newPinText.length == 4) newPinText else null
                        mainViewModel.updatePin(pin)
                        showPinDialog = false
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Help & FAQ Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help Center & FAQ") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. How do daily expense reminders work?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "CediWise schedules local system alarms at your chosen Morning and Evening times. Reminders work reliably even when the app is completely closed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "2. How do I record Mobile Money (MoMo)?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Tap '+ Add Expense', choose category (e.g., MoMo, Food, Trotro) and select 'MTN MoMo', 'Telecel Cash', or 'AT Money' under Payment Method.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "3. Can I export my financial records?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Yes! Tap 'Export Financial Records (CSV)' above to share or download a spreadsheet-ready ledger via email, WhatsApp, or file storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("Close") }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Last updated: September 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• On-Device Encrypted Storage: All your recorded transactions, income, expense categories, budgets, and savings goals are stored locally in an encrypted Room SQLite database on your device.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Zero Data Sharing: We do not sell, rent, monetize, or transmit your financial information or personal data to any third-party advertisers or data brokers.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Secure Authentication: Passwords are protected using cryptographic SHA-256 hashing. Optional 4-digit App PIN lock ensures your financial ledger remains private from anyone with access to your device.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Data Retention & Account Deletion: You retain 100% ownership of your records. You may export all transactions to CSV or permanently delete your account and all associated data at any time via the 'Delete Account' button below.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("I Agree") }
            }
        )
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "• Personal Financial Management: CediWise is a personal expense tracking and budgeting tool designed to help you track personal cashflow and adhere to spending budgets.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Not a Banking Institution: CediWise does not hold, transmit, custody, or manage banking funds. Transactions recorded in CediWise are for personal bookkeeping purposes.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Estimation & Budget Warnings: Projections, budget percentages, and spending estimates are informational tools calculated based strictly on your recorded entries.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("Understood") }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About CediWise") },
            text = {
                Column {
                    Text("CediWise v1.0.0")
                    Text("A production-ready personal expense tracker designed primarily for Ghana and international users.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Features include automatic daily expense reminders, budget monitoring, recurring expenses, bill alerts, and multi-currency reporting.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account & Data?") },
            text = {
                Text("This action is permanent and irreversible. All your transactions, budgets, savings goals, bills, and profile data will be permanently wiped from the database.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.deleteAccount {
                            showDeleteAccountDialog = false
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
