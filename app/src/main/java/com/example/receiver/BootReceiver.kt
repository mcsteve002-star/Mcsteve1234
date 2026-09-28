package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.preferences.PreferenceManager
import com.example.notification.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefManager = PreferenceManager(context)
            if (prefManager.isLoggedIn()) {
                AlarmScheduler.scheduleDailyReminders(context)
            }
        }
    }
}
