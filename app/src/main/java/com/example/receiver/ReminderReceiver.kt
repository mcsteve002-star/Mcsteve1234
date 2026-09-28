package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.CediWiseDatabase
import com.example.data.preferences.PreferenceManager
import com.example.notification.NotificationHelper
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefManager = PreferenceManager(context)
        val currentUserId = prefManager.getCurrentUserId()
        if (currentUserId <= 0) return // No logged in user

        when (intent?.action) {
            ACTION_MORNING_REMINDER -> {
                NotificationHelper.showNotification(
                    context = context,
                    channelId = NotificationHelper.CHANNEL_REMINDERS,
                    notificationId = 101,
                    title = "Good morning! ☀️",
                    message = "Remember to record your expenses today and stay on track with your budget."
                )
            }
            ACTION_EVENING_REMINDER -> {
                NotificationHelper.showNotification(
                    context = context,
                    channelId = NotificationHelper.CHANNEL_REMINDERS,
                    notificationId = 102,
                    title = "Daily Expense Check-in 🌙",
                    message = "Have you recorded all your expenses for today? Take a moment to update CediWise."
                )
            }
            ACTION_BILL_CHECK -> {
                val db = CediWiseDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {
                    val oneDayAhead = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                    val upcomingBills = db.billReminderDao().getUpcomingUnpaidBillsSync(currentUserId, oneDayAhead)
                    val user = db.userDao().getUserById(currentUserId)
                    val currency = user?.preferredCurrency ?: "GHS"

                    for ((idx, bill) in upcomingBills.take(3).withIndex()) {
                        NotificationHelper.showNotification(
                            context = context,
                            channelId = NotificationHelper.CHANNEL_BILLS,
                            notificationId = 200 + idx,
                            title = "Upcoming Bill Due ⏰",
                            message = "${bill.title} of ${CurrencyFormatter.format(bill.amount, currency)} is due soon!"
                        )
                    }
                }
            }
            ACTION_BUDGET_WARNING -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Budget Alert"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have reached your budget limit."
                NotificationHelper.showNotification(
                    context = context,
                    channelId = NotificationHelper.CHANNEL_BUDGETS,
                    notificationId = 301,
                    title = title,
                    message = message
                )
            }
        }
    }

    companion object {
        const val ACTION_MORNING_REMINDER = "com.example.cediwise.ACTION_MORNING_REMINDER"
        const val ACTION_EVENING_REMINDER = "com.example.cediwise.ACTION_EVENING_REMINDER"
        const val ACTION_BILL_CHECK = "com.example.cediwise.ACTION_BILL_CHECK"
        const val ACTION_BUDGET_WARNING = "com.example.cediwise.ACTION_BUDGET_WARNING"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }
}
