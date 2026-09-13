package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class AppCategoryItem(
    val key: String,
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val pastelBg: Color,
    val isCustom: Boolean = false,
    val iconKey: String = "Category",
    val customId: Long? = null,
    val isIncome: Boolean = false
) {
    val name: String get() = key
}

object CategoryRegistry {
    private val cachedCustom = mutableMapOf<String, CustomCategory>()

    fun updateCustomCategories(list: List<CustomCategory>) {
        cachedCustom.clear()
        list.forEach {
            cachedCustom[it.name.trim().lowercase()] = it
        }
    }

    fun getAllCategoryItems(): List<AppCategoryItem> {
        val list = mutableListOf<AppCategoryItem>()
        // 1. Standard categories
        ExpenseCategory.entries.forEach { std ->
            list.add(getCategoryItem(std.name))
        }
        // 2. Custom categories
        cachedCustom.values.forEach { custom ->
            list.add(getCategoryItem(custom.name))
        }
        return list
    }

    fun getIncomeCategoryItem(): AppCategoryItem {
        return getCategoryItem(ExpenseCategory.INCOME.name)
    }

    fun getExpenseCategoryItems(): List<AppCategoryItem> {
        return getAllCategoryItems()
            .filter {
                !it.isIncome &&
                !it.key.equals(ExpenseCategory.INCOME.name, ignoreCase = true) &&
                !it.displayName.equals("Income", ignoreCase = true) &&
                !it.key.equals("SALARY", ignoreCase = true)
            }
            .distinctBy { it.displayName.lowercase() }
    }

    fun getCategoryItem(categoryName: String?): AppCategoryItem {
        if (categoryName.isNullOrBlank()) {
            val other = ExpenseCategory.OTHER
            return AppCategoryItem(
                key = other.name,
                displayName = other.displayName,
                icon = other.icon,
                color = other.color,
                pastelBg = Color(0xFFF1F5F9),
                isCustom = false,
                isIncome = false
            )
        }

        val clean = categoryName.trim()
        val custom = cachedCustom[clean.lowercase()]
        if (custom != null) {
            val catColor = custom.color
            return AppCategoryItem(
                key = custom.name,
                displayName = custom.name,
                icon = custom.icon,
                color = catColor,
                pastelBg = catColor.copy(alpha = 0.15f),
                isCustom = true,
                iconKey = custom.iconKey,
                customId = custom.id,
                isIncome = false
            )
        }

        // Try standard ExpenseCategory
        val standard = ExpenseCategory.fromString(clean)
        val pastel = when (standard) {
            ExpenseCategory.FOOD -> Color(0xFFE0F2FE)
            ExpenseCategory.TRANSPORT -> Color(0xFFF3E8FF)
            ExpenseCategory.SHOPPING -> Color(0xFFFFEDD5)
            ExpenseCategory.ENTERTAINMENT -> Color(0xFFEDE9FE)
            ExpenseCategory.HEALTH -> Color(0xFFDCFCE7)
            ExpenseCategory.RENT -> Color(0xFFFEE2E2)
            ExpenseCategory.UTILITIES -> Color(0xFFFEF3C7)
            ExpenseCategory.INCOME -> Color(0xFFDCFCE7)
            ExpenseCategory.OTHER -> Color(0xFFF1F5F9)
        }

        val isCategoryIncome = standard == ExpenseCategory.INCOME ||
                clean.equals("INCOME", ignoreCase = true) ||
                clean.equals("SALARY", ignoreCase = true)

        return AppCategoryItem(
            key = standard.name,
            displayName = if (standard == ExpenseCategory.OTHER && !clean.equals("OTHER", ignoreCase = true)) clean else standard.displayName,
            icon = standard.icon,
            color = standard.color,
            pastelBg = pastel,
            isCustom = false,
            isIncome = isCategoryIncome
        )
    }
}
