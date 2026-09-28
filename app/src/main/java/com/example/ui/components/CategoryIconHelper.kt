package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(iconName: String, categoryName: String): ImageVector {
        val lower = categoryName.lowercase()
        return when {
            lower.contains("food") || lower.contains("dining") || lower.contains("meal") -> Icons.Default.Restaurant
            lower.contains("trotro") || lower.contains("transport") || lower.contains("bus") -> Icons.Default.DirectionsBus
            lower.contains("momo") || lower.contains("wallet") -> Icons.Default.AccountBalanceWallet
            lower.contains("airtime") || lower.contains("data") || lower.contains("wifi") || lower.contains("internet") -> Icons.Default.Wifi
            lower.contains("fuel") || lower.contains("gas") -> Icons.Default.LocalGasStation
            lower.contains("rent") || lower.contains("housing") || lower.contains("apartment") -> Icons.Default.Home
            lower.contains("util") || lower.contains("electric") || lower.contains("water") -> Icons.Default.Bolt
            lower.contains("school") || lower.contains("edu") || lower.contains("fee") -> Icons.Default.School
            lower.contains("health") || lower.contains("med") || lower.contains("hospital") -> Icons.Default.MedicalServices
            lower.contains("church") || lower.contains("tithe") || lower.contains("donat") -> Icons.Default.VolunteerActivism
            lower.contains("family") || lower.contains("relative") -> Icons.Default.Diversity1
            lower.contains("shop") || lower.contains("grocer") || lower.contains("market") -> Icons.Default.ShoppingCart
            lower.contains("entertain") || lower.contains("party") || lower.contains("outing") -> Icons.Default.Nightlife
            lower.contains("spa") || lower.contains("personal") || lower.contains("barber") || lower.contains("hair") -> Icons.Default.Spa
            lower.contains("subscript") || lower.contains("netflix") || lower.contains("spotify") -> Icons.Default.Subscriptions
            lower.contains("store") || lower.contains("business") || lower.contains("trade") -> Icons.Default.Storefront
            lower.contains("salary") || lower.contains("wage") || lower.contains("job") -> Icons.Default.Work
            lower.contains("sales") || lower.contains("client") -> Icons.Default.Payments
            lower.contains("freelance") || lower.contains("gig") || lower.contains("laptop") -> Icons.Default.Laptop
            lower.contains("allowance") -> Icons.Default.CardGiftcard
            lower.contains("gift") -> Icons.Default.Redeem
            lower.contains("invest") || lower.contains("dividend") -> Icons.AutoMirrored.Filled.TrendingUp
            else -> Icons.Default.Category
        }
    }

    fun getColor(colorHex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (_: Exception) {
            Color(0xFF00897B)
        }
    }
}
