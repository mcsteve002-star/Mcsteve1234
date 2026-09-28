package com.example.ui.screens.onboarding

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.UserEntity
import com.example.data.model.Currencies
import com.example.data.preferences.PreferenceManager
import com.example.data.repository.CediWiseRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    user: UserEntity,
    repository: CediWiseRepository,
    preferenceManager: PreferenceManager,
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }

    // User responses
    var country by remember { mutableStateOf(user.country.ifBlank { "Ghana" }) }
    var currencyCode by remember { mutableStateOf(user.preferredCurrency.ifBlank { "GHS" }) }
    var budgetPeriod by remember { mutableStateOf("MONTHLY") }
    var monthlyIncomeText by remember { mutableStateOf("") }

    val categories = listOf(
        "Food & Dining", "Trotro / Transport", "MoMo Transfers",
        "Airtime & Data", "Fuel & Vehicle", "Rent & Housing",
        "School Fees", "Healthcare", "Church & Tithes", "Family Support", "Groceries"
    )
    val selectedCategories = remember {
        mutableStateListOf("Food & Dining", "Trotro / Transport", "MoMo Transfers", "Airtime & Data")
    }

    var enableDailyReminders by remember { mutableStateOf(true) }
    var morningHour by remember { mutableStateOf(8) }
    var eveningHour by remember { mutableStateOf(20) }

    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    fun saveAndFinish() {
        val incomeVal = monthlyIncomeText.toDoubleOrNull() ?: 0.0
        val updatedUser = user.copy(
            country = country,
            preferredCurrency = currencyCode,
            monthlyIncome = incomeVal,
            preferredBudgetPeriod = budgetPeriod,
            dailyRemindersEnabled = enableDailyReminders,
            morningReminderHour = morningHour,
            eveningReminderHour = eveningHour
        )
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateUser(updatedUser)
            if (enableDailyReminders) {
                AlarmScheduler.scheduleDailyReminders(context, morningHour, 0, eveningHour, 0)
            }
        }
        preferenceManager.setFirstLaunchCompleted()
        onFinishOnboarding()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))

                // Step indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { step ->
                        val active = step + 1 == currentStep
                        val completed = step + 1 < currentStep
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (active) 24.dp else 12.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        active -> MaterialTheme.colorScheme.primary
                                        completed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (currentStep) {
                    1 -> {
                        // Step 1: Welcome & Currency
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(16.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.onboarding_hero),
                                contentDescription = "Financial Freedom Illustration",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Welcome to CediWise, ${user.fullName.split(" ").firstOrNull() ?: ""}!",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Let's personalize your financial dashboard in 60 seconds.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Currency Selector
                        ExposedDropdownMenuBox(
                            expanded = currencyDropdownExpanded,
                            onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val curr = Currencies.fromCode(currencyCode)
                            OutlinedTextField(
                                value = "${curr.flag} ${curr.code} - ${curr.name} (${curr.symbol})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Primary Currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .testTag("onboarding_currency_select"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false }
                            ) {
                                Currencies.all.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.flag} ${c.code} - ${c.name} (${c.symbol})") },
                                        onClick = {
                                            currencyCode = c.code
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Country of Residence") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    2 -> {
                        // Step 2: Budgeting & Income
                        Text(
                            text = "Budgeting & Spending Style",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "How do you prefer to manage and monitor your spending?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Preferred Budget Cycle",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("DAILY", "WEEKLY", "MONTHLY").forEach { period ->
                                FilterChip(
                                    selected = budgetPeriod == period,
                                    onClick = { budgetPeriod = period },
                                    label = { Text(period.lowercase().replaceFirstChar { it.uppercase() }) },
                                    leadingIcon = if (budgetPeriod == period) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val curr = Currencies.fromCode(currencyCode)
                        OutlinedTextField(
                            value = monthlyIncomeText,
                            onValueChange = { monthlyIncomeText = it },
                            label = { Text("Estimated Monthly Income (Optional)") },
                            prefix = { Text("${curr.symbol} ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Select typical spending categories:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategories.contains(cat)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedCategories.remove(cat)
                                        else selectedCategories.add(cat)
                                    },
                                    label = { Text(cat) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else null
                                )
                            }
                        }
                    }

                    3 -> {
                        // Step 3: Notification Reminders
                        Text(
                            text = "Never Forget an Expense",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Automatic reminders help 92% of users stay disciplined with their budget.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.size(12.dp))
                                        Column {
                                            Text(
                                                text = "Daily Expense Reminders",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Text(
                                                text = "Morning & Evening prompts",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = enableDailyReminders,
                                        onCheckedChange = { enableDailyReminders = it },
                                        modifier = Modifier.testTag("onboarding_reminder_toggle")
                                    )
                                }

                                if (enableDailyReminders) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Morning Reminder", style = MaterialTheme.typography.bodyMedium)
                                            Text("08:00 AM", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                        }
                                        Column {
                                            Text("Evening Reminder", style = MaterialTheme.typography.bodyMedium)
                                            Text("08:00 PM (20:00)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "CediWise operates fully offline on your device with high-grade local security.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            saveAndFinish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentStep < 3) "Continue" else "Go to Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (currentStep < 3) {
                    TextButton(
                        onClick = { saveAndFinish() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Skip setup for now",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
