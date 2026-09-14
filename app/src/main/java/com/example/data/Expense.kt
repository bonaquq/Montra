package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.CategoryEntertainment
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryIncome
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryPurchase
import com.example.ui.theme.CategoryRent
import com.example.ui.theme.CategorySalary
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryTransfer
import com.example.ui.theme.CategoryTransport
import com.example.ui.theme.CategoryUtilities

enum class ExpenseCategory(
    val displayName: String,
    val color: Color
) {
    PURCHASE("Purchase", CategoryPurchase),
    TRANSFER("Transfer", CategoryTransfer),
    RENT("Rent", CategoryRent),
    FOOD("Food & Drink", CategoryFood),
    TRANSPORT("Transport", CategoryTransport),
    SHOPPING("Shopping", CategoryShopping),
    ENTERTAINMENT("Entertainment", CategoryEntertainment),
    UTILITIES("Utilities", CategoryUtilities),
    HEALTH("Health & Wellness", CategoryHealth),
    INCOME("Income", CategoryIncome),
    OTHER("Other", CategoryOther);

    val isIncomeCategory: Boolean
        get() = this == INCOME

    val icon: ImageVector
        get() = when (this) {
            PURCHASE -> Icons.Filled.PointOfSale
            TRANSFER -> Icons.AutoMirrored.Filled.CompareArrows
            RENT -> Icons.Filled.Home
            FOOD -> Icons.Filled.Restaurant
            TRANSPORT -> Icons.Filled.DirectionsBus
            SHOPPING -> Icons.Filled.ShoppingBag
            ENTERTAINMENT -> Icons.Filled.SportsEsports
            UTILITIES -> Icons.Filled.Bolt
            INCOME -> Icons.AutoMirrored.Filled.TrendingUp
            HEALTH -> Icons.Filled.FitnessCenter
            OTHER -> Icons.Filled.MoreHoriz
        }

    companion object {
        fun fromString(name: String?): ExpenseCategory {
            if (name == null) return OTHER
            val trimmed = name.trim()
            return when (trimmed.uppercase()) {
                "BILLS" -> UTILITIES
                "SALARY" -> INCOME
                "INCOME" -> INCOME
                "PURCHASE", "PURCHASES", "POS" -> PURCHASE
                "TRANSFER", "TRANSFERS", "FUND TRANSFER" -> TRANSFER
                else -> entries.firstOrNull { 
                    it.name.equals(trimmed, ignoreCase = true) || 
                    it.displayName.equals(trimmed, ignoreCase = true) 
                } ?: OTHER
            }
        }

        // Smart Categorization AI/Heuristics
        fun predictCategory(text: String): ExpenseCategory {
            val lower = text.lowercase()
            return when {
                lower.contains("salary") || lower.contains("paycheck") ||
                lower.contains("freelance") || lower.contains("bonus") ||
                lower.contains("payroll") || lower.contains("allowance") -> INCOME

                lower.contains("transfer") || lower.contains("transferred") || lower.contains("fund transfer") ||
                lower.contains("internet banking") || lower.contains("faisamobile") || lower.contains("faisanet") -> TRANSFER

                lower.contains("purchase") || lower.contains("pos purchase") || lower.contains("mart") ||
                lower.contains("store purchase") || lower.contains("debit card purchase") -> PURCHASE

                lower.contains("rent") || lower.contains("lease") || lower.contains("landlord") ||
                lower.contains("apartment") || lower.contains("housing") -> RENT

                lower.contains("uber") || lower.contains("lyft") || lower.contains("gas") ||
                lower.contains("fuel") || lower.contains("parking") || lower.contains("transit") ||
                lower.contains("metro") || lower.contains("train") || lower.contains("subway") ||
                lower.contains("flight") || lower.contains("bus") || lower.contains("taxi") ||
                lower.contains("fsm") || lower.contains("petrol") || lower.contains("mtcc") ||
                lower.contains("ferry") || lower.contains("raajje transport") -> TRANSPORT

                lower.contains("coffee") || lower.contains("starbucks") || lower.contains("cafe") ||
                lower.contains("restaurant") || lower.contains("dinner") || lower.contains("lunch") ||
                lower.contains("breakfast") || lower.contains("burger") || lower.contains("pizza") ||
                lower.contains("groceries") || lower.contains("market") || lower.contains("supermarket") ||
                lower.contains("trader joe") || lower.contains("whole foods") ||
                lower.contains("seagull") || lower.contains("dinemore") || lower.contains("shell beans") ||
                lower.contains("coffee club") || lower.contains("marrybrown") ||
                lower.contains("food") || lower.contains("drink") || lower.contains("bakery") -> FOOD

                lower.contains("amazon") || lower.contains("walmart") || lower.contains("target") ||
                lower.contains("shoes") || lower.contains("clothes") || lower.contains("shirt") ||
                lower.contains("clothing") || lower.contains("apple store") || lower.contains("electronics") ||
                lower.contains("agora") || lower.contains("redwave") || lower.contains("fantasy") ||
                lower.contains("ihsan") || lower.contains("sonee") || lower.contains("veligaa") ||
                lower.contains("asters") || lower.contains("sto supermarket") ||
                lower.contains("mall") || lower.contains("store") || lower.contains("shop") -> SHOPPING

                lower.contains("electric") || lower.contains("water") || lower.contains("utility") ||
                lower.contains("internet") || lower.contains("wifi") || lower.contains("phone") ||
                lower.contains("power") || lower.contains("bill") || lower.contains("verizon") ||
                lower.contains("stelco") || lower.contains("mwsc") || lower.contains("dhiraagu") ||
                lower.contains("ooredoo") || lower.contains("medianet") || lower.contains("wamco") -> UTILITIES

                lower.contains("netflix") || lower.contains("spotify") || lower.contains("disney") ||
                lower.contains("cinema") || lower.contains("movie") || lower.contains("concert") ||
                lower.contains("steam") || lower.contains("game") || lower.contains("entertainment") -> ENTERTAINMENT

                lower.contains("pharmacy") || lower.contains("gym") || lower.contains("fitness") ||
                lower.contains("doctor") || lower.contains("dentist") || lower.contains("hospital") ||
                lower.contains("adk") || lower.contains("tree top") || lower.contains("igmh") ||
                lower.contains("medica") || lower.contains("sto pharmacy") ||
                lower.contains("medicine") || lower.contains("clinic") || lower.contains("health") -> HEALTH

                else -> OTHER
            }
        }
    }
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // Stored as enum name e.g. "FOOD"
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val currencyCode: String = "USD",
    val receiptUri: String? = null,
    val isAutomated: Boolean = false,
    val transactionType: String = "EXPENSE" // "EXPENSE" or "INCOME"
) {
    val expenseCategory: ExpenseCategory
        get() = ExpenseCategory.fromString(category)

    val currency: SupportedCurrency
        get() = SupportedCurrency.fromCode(currencyCode)

    val isIncome: Boolean
        get() = transactionType.equals("INCOME", ignoreCase = true) ||
                expenseCategory == ExpenseCategory.INCOME ||
                category.equals("INCOME", ignoreCase = true) ||
                category.equals("SALARY", ignoreCase = true)
}
