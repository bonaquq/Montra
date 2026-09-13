package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryRegistry
import com.example.data.CustomCategory
import com.example.data.ExpenseCategory
import com.example.data.SupportedCurrency
import com.example.ui.ExpenseUiState
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import com.example.util.FormatUtils
import java.util.Locale

data class CategoryDisplayModel(
    val key: String,
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val pastelBg: Color,
    val totalAmount: Double,
    val percentage: Float,
    val isCustom: Boolean = false,
    val customCategory: CustomCategory? = null,
    val standardCategory: ExpenseCategory? = null
)

@Composable
fun CategoriesScreenContent(
    uiState: ExpenseUiState,
    onAddCategory: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onDeleteCustomCategory: (CustomCategory) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Calculate spending per category dynamically
    val currency = uiState.selectedCurrency
    val nonIncomeExpenses = remember(uiState.allExpensesUnfiltered, currency) {
        uiState.allExpensesUnfiltered.filter { !it.isIncome }
    }

    val totalSpent = remember(nonIncomeExpenses, currency) {
        nonIncomeExpenses.sumOf {
            SupportedCurrency.convert(it.amount, it.currency, currency)
        }
    }

    val allCategoryItems = remember(uiState.customCategories, nonIncomeExpenses, totalSpent) {
        val items = mutableListOf<CategoryDisplayModel>()

        // 1. Standard categories (Expense categories only)
        ExpenseCategory.entries.filter { !it.isIncomeCategory }.forEach { std ->
            val spentInCat = nonIncomeExpenses.filter { exp ->
                exp.category.equals(std.name, ignoreCase = true) ||
                exp.category.equals(std.displayName, ignoreCase = true)
            }.sumOf {
                SupportedCurrency.convert(it.amount, it.currency, currency)
            }
            val pct = if (totalSpent > 0) ((spentInCat / totalSpent) * 100f).toFloat() else 0f

            val itemInfo = CategoryRegistry.getCategoryItem(std.name)
            items.add(
                CategoryDisplayModel(
                    key = std.name,
                    displayName = std.displayName,
                    icon = itemInfo.icon,
                    color = itemInfo.color,
                    pastelBg = itemInfo.pastelBg,
                    totalAmount = spentInCat,
                    percentage = pct,
                    isCustom = false,
                    standardCategory = std
                )
            )
        }

        // 2. Custom categories created by user
        uiState.customCategories.forEach { custom ->
            val spentInCat = nonIncomeExpenses.filter { exp ->
                exp.category.equals(custom.name, ignoreCase = true)
            }.sumOf {
                SupportedCurrency.convert(it.amount, it.currency, currency)
            }
            val pct = if (totalSpent > 0) ((spentInCat / totalSpent) * 100f).toFloat() else 0f

            val itemInfo = CategoryRegistry.getCategoryItem(custom.name)
            items.add(
                CategoryDisplayModel(
                    key = custom.name,
                    displayName = custom.name,
                    icon = itemInfo.icon,
                    color = itemInfo.color,
                    pastelBg = itemInfo.pastelBg,
                    totalAmount = spentInCat,
                    percentage = pct,
                    isCustom = true,
                    customCategory = custom
                )
            )
        }

        // Sort so custom categories or highest spending categories are easily seen
        items
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MontraBackground)
            .statusBarsPadding()
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Categories",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MontraTextPrimary,
                modifier = Modifier.testTag("txt_categories_title")
            )
            Text(
                text = "${allCategoryItems.size} categories • ${uiState.customCategories.size} custom",
                fontSize = 13.sp,
                color = MontraTextSecondary,
                modifier = Modifier.testTag("txt_categories_count")
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Create your own category" prompt card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onAddCategory() }
                        .testTag("card_create_custom_category_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MontraSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Create Custom Category",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontraTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Pick your own icon, color, and name",
                                    fontSize = 12.sp,
                                    color = MontraTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2563EB))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+ New",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Categories Section Title
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ALL CATEGORIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MontraTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Category rows
            items(allCategoryItems, key = { it.key }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MontraSurface)
                        .clickable { onCategoryClick(item.key) }
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                        .testTag("category_row_${item.key}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(item.pastelBg)
                                .border(1.dp, item.color.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.displayName,
                                tint = item.color,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MontraTextPrimary
                                )
                                if (item.isCustom) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(item.color.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Custom",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = item.color
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FormatUtils.formatCurrency(item.totalAmount, currency),
                                fontSize = 13.sp,
                                color = MontraTextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f%%", item.percentage),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MontraTextSecondary
                        )

                        if (item.isCustom && item.customCategory != null) {
                            IconButton(
                                onClick = { onDeleteCustomCategory(item.customCategory) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_delete_category_${item.key}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete Category",
                                    tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "View",
                                tint = MontraTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
