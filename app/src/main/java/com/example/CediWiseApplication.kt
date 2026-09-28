package com.example

import android.app.Application
import com.example.data.local.CediWiseDatabase
import com.example.data.preferences.PreferenceManager
import com.example.data.repository.CediWiseRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper

class CediWiseApplication : Application() {
    val database: CediWiseDatabase by lazy { CediWiseDatabase.getDatabase(this) }
    val repository: CediWiseRepository by lazy { CediWiseRepository(database) }
    val preferenceManager: PreferenceManager by lazy { PreferenceManager(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        if (preferenceManager.isLoggedIn()) {
            AlarmScheduler.scheduleDailyReminders(this)
        }
    }
}
