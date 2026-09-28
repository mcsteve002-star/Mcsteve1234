package com.example.data.model

data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String = ""
)

object Currencies {
    val GHS = Currency("GHS", "GH₵", "Ghanaian Cedi", "🇬🇭")
    val USD = Currency("USD", "$", "US Dollar", "🇺🇸")
    val GBP = Currency("GBP", "£", "British Pound", "🇬🇧")
    val EUR = Currency("EUR", "€", "Euro", "🇪🇺")
    val NGN = Currency("NGN", "₦", "Nigerian Naira", "🇳🇬")
    val KES = Currency("KES", "KSh", "Kenyan Shilling", "🇰🇪")
    val ZAR = Currency("ZAR", "R", "South African Rand", "🇿🇦")
    val CAD = Currency("CAD", "CA$", "Canadian Dollar", "🇨🇦")

    val all = listOf(GHS, USD, GBP, EUR, NGN, KES, ZAR, CAD)

    fun fromCode(code: String): Currency {
        return all.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: GHS
    }
}
