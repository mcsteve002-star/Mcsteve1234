package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cediwise_preferences", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow(getCurrentUserId())
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun getCurrentUserId(): Long {
        return prefs.getLong(KEY_CURRENT_USER_ID, -1L)
    }

    fun setCurrentUserId(userId: Long) {
        prefs.edit().putLong(KEY_CURRENT_USER_ID, userId).apply()
        _currentUserId.value = userId
    }

    fun isLoggedIn(): Boolean {
        return getCurrentUserId() > 0
    }

    fun logout() {
        prefs.edit().putLong(KEY_CURRENT_USER_ID, -1L).apply()
        _currentUserId.value = -1L
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
