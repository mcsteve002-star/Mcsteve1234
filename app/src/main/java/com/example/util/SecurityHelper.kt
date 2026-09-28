package com.example.util

import java.security.MessageDigest

object SecurityHelper {
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }

    fun isPasswordStrong(password: String): Pair<Boolean, String> {
        if (password.length < 6) {
            return Pair(false, "Password must be at least 6 characters")
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return Pair(false, "Password should contain both letters and numbers")
        }
        return Pair(true, "")
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.isNotBlank() && emailRegex.matches(email.trim())
    }
}
