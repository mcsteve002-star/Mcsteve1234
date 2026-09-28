package com.example

import com.example.data.model.Currencies
import com.example.util.CurrencyFormatter
import com.example.util.SecurityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        val formattedGhs = CurrencyFormatter.format(1250.50, "GHS")
        assertTrue(formattedGhs.contains("GH₵") || formattedGhs.contains("1,250.50"))

        val formattedUsd = CurrencyFormatter.format(500.00, "USD")
        assertTrue(formattedUsd.contains("$") && formattedUsd.contains("500.00"))
    }

    @Test
    fun testPasswordStrengthValidation() {
        val (weak, _) = SecurityHelper.isPasswordStrong("123")
        assertFalse(weak)

        val (strong, _) = SecurityHelper.isPasswordStrong("Password123")
        assertTrue(strong)
    }

    @Test
    fun testPasswordHashingAndVerification() {
        val password = "MySecurePassword2026"
        val hash = SecurityHelper.hashPassword(password)
        assertTrue(SecurityHelper.verifyPassword(password, hash))
        assertFalse(SecurityHelper.verifyPassword("WrongPassword", hash))
    }

    @Test
    fun testCurrenciesCatalog() {
        val ghs = Currencies.fromCode("GHS")
        assertEquals("GHS", ghs.code)
        assertEquals("GH₵", ghs.symbol)

        val usd = Currencies.fromCode("USD")
        assertEquals("USD", usd.code)
    }

    @Test
    fun testEmailValidation() {
        assertTrue(SecurityHelper.isValidEmail("kwame@cediwise.com"))
        assertFalse(SecurityHelper.isValidEmail("invalid-email"))
    }
}
