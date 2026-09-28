package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.receiver.ReminderReceiver
import java.util.Calendar

object AlarmScheduler {
    const val REQUEST_CODE_MORNING = 1001
    const val REQUEST_CODE_EVENING = 1002
    const val REQUEST_CODE_BILLS = 1003

    fun scheduleDailyReminders(
        context: Context,
        morningHour: Int = 8,
        morningMinute: Int = 0,
        eveningHour: Int = 20,
        eveningMinute: Int = 0
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Morning reminder
        scheduleDailyAlarm(
            context,
            alarmManager,
            REQUEST_CODE_MORNING,
            morningHour,
            morningMinute,
            ReminderReceiver.ACTION_MORNING_REMINDER
        )

        // Evening reminder
        scheduleDailyAlarm(
            context,
            alarmManager,
            REQUEST_CODE_EVENING,
            eveningHour,
            eveningMinute,
            ReminderReceiver.ACTION_EVENING_REMINDER
        )

        // Midday bill reminder check (12:00)
        scheduleDailyAlarm(
            context,
            alarmManager,
            REQUEST_CODE_BILLS,
            12,
            0,
            ReminderReceiver.ACTION_BILL_CHECK
        )
    }

    private fun scheduleDailyAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        hour: Int,
        minute: Int,
        action: String
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // Permission or restricted scheduling
        }
    }

    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val codes = listOf(REQUEST_CODE_MORNING, REQUEST_CODE_EVENING, REQUEST_CODE_BILLS)
        for (code in codes) {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}
