package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.UserEntity
import com.example.data.preferences.PreferenceManager
import com.example.data.repository.CediWiseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserEntity) : AuthState()
    data class NeedsPin(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: CediWiseRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        val userId = preferenceManager.getCurrentUserId()
        if (userId > 0) {
            viewModelScope.launch {
                val user = repository.getUser(userId)
                if (user != null) {
                    _currentUser.value = user
                    if (!user.appLockPin.isNullOrBlank()) {
                        _authState.value = AuthState.NeedsPin(user)
                    } else {
                        _authState.value = AuthState.Authenticated(user)
                    }
                } else {
                    preferenceManager.logout()
                    _authState.value = AuthState.Idle
                }
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun login(email: String, passwordPlain: String) {
        if (email.isBlank() || passwordPlain.isBlank()) {
            _authState.value = AuthState.Error("Please enter both email and password")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, passwordPlain)
            result.fold(
                onSuccess = { user ->
                    preferenceManager.setCurrentUserId(user.id)
                    _currentUser.value = user
                    if (!user.appLockPin.isNullOrBlank()) {
                        _authState.value = AuthState.NeedsPin(user)
                    } else {
                        _authState.value = AuthState.Authenticated(user)
                    }
                },
                onFailure = { err ->
                    _authState.value = AuthState.Error(err.message ?: "Authentication failed")
                }
            )
        }
    }

    fun register(
        fullName: String,
        email: String,
        passwordPlain: String,
        confirmPasswordPlain: String,
        phone: String,
        country: String,
        currency: String
    ) {
        if (fullName.isBlank() || email.isBlank() || passwordPlain.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields")
            return
        }
        if (passwordPlain != confirmPasswordPlain) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.registerUser(
                fullName = fullName,
                email = email,
                passwordPlain = passwordPlain,
                phone = phone,
                country = country,
                currency = currency
            )
            result.fold(
                onSuccess = { user ->
                    preferenceManager.setCurrentUserId(user.id)
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { err ->
                    _authState.value = AuthState.Error(err.message ?: "Registration failed")
                }
            )
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        val user = _currentUser.value ?: return false
        if (user.appLockPin == enteredPin) {
            _authState.value = AuthState.Authenticated(user)
            return true
        }
        return false
    }

    fun resetPassword(email: String, newPass: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.resetPassword(email, newPass)
            res.fold(
                onSuccess = { onDone(true, "Password updated successfully! You can now log in.") },
                onFailure = { onDone(false, it.message ?: "Failed to reset password") }
            )
        }
    }

    fun logout() {
        preferenceManager.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}
