package com.example.util

import com.example.data.model.Currencies
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val wholeFormat = DecimalFormat("#,##0")

    fun format(amount: Double, currencyCode: String = "GHS"): String {
        val curr = Currencies.fromCode(currencyCode)
        val formattedNumber = decimalFormat.format(amount)
        return "${curr.symbol}$formattedNumber"
    }

    fun formatCompact(amount: Double, currencyCode: String = "GHS"): String {
        val curr = Currencies.fromCode(currencyCode)
        return when {
            kotlin.math.abs(amount) >= 1_000_000 -> {
                val millions = amount / 1_000_000.0
                "${curr.symbol}${decimalFormat.format(millions)}M"
            }
            kotlin.math.abs(amount) >= 1_000 -> {
                val thousands = amount / 1_000.0
                "${curr.symbol}${decimalFormat.format(thousands)}K"
            }
            else -> format(amount, currencyCode)
        }
    }

    fun formatPercentage(percent: Double): String {
        return "${decimalFormat.format(percent)}%"
    }
}
