package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryRegistry
import com.example.data.SupportedCurrency
import com.example.ui.BudgetStatus
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import com.example.util.AmountInputUtils
import com.example.util.FormatUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBudgetsSheet(
    budgetStatuses: List<BudgetStatus>,
    currency: SupportedCurrency,
    onSaveBudget: (String, Double) -> Unit,
    onDeleteBudget: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Pre-populate overall limit
    val overallLimit = budgetStatuses.firstOrNull { it.categoryName.equals("OVERALL", ignoreCase = true) }?.monthlyLimit ?: 1500.0
    var overallInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", overallLimit)) }

    // All category items (standard + custom)
    val allCategories = remember { CategoryRegistry.getExpenseCategoryItems() }

    val categoryInputs = remember {
        mutableStateMapOf<String, String>().apply {
            allCategories.forEach { cat ->
                val current = budgetStatuses.firstOrNull { 
                    it.categoryName.equals(cat.key, ignoreCase = true) || 
                    it.categoryName.equals(cat.displayName, ignoreCase = true) 
                }?.monthlyLimit
                put(cat.key, if (current != null && current > 0) String.format(Locale.US, "%.0f", current) else "")
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MontraBackground,
        dragHandle = null,
        modifier = Modifier.testTag("manage_budgets_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Monthly Budgets",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                        Text(
                            text = "Set overall caps & category limits (${currency.code})",
                            fontSize = 12.sp,
                            color = MontraTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_budgets_sheet_button")
                ) {
                    Icon(
                        Icons.Default.Close, 
                        contentDescription = "Close",
                        tint = MontraTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overall Monthly Cap Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MontraSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Overall Monthly Cap",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8)
                                )
                                val currentOverall = budgetStatuses.firstOrNull { it.categoryName.equals("OVERALL", ignoreCase = true) }
                                if (currentOverall != null) {
                                    Text(
                                        text = "Spent: ${FormatUtils.formatCurrency(currentOverall.currentSpent, currency)}",
                                        fontSize = 12.sp,
                                        color = MontraTextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = overallInput,
                                onValueChange = { overallInput = AmountInputUtils.sanitizeAmount(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("budget_overall_input"),
                                prefix = { Text("${currency.symbol} ", color = MontraTextPrimary, fontWeight = FontWeight.Bold) },
                                label = { Text("Total Monthly Limit", color = MontraTextSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = MontraBorder,
                                    focusedTextColor = MontraTextPrimary,
                                    unfocusedTextColor = MontraTextPrimary
                                )
                            )
                        }
                    }
                }

                // Category Allowances Section Header
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CATEGORY ALLOWANCES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextMuted,
                        letterSpacing = 1.sp
                    )
                }

                // Category rows
                items(allCategories, key = { it.key }) { cat ->
                    val status = budgetStatuses.firstOrNull { 
                        it.categoryName.equals(cat.key, ignoreCase = true) || 
                        it.categoryName.equals(cat.displayName, ignoreCase = true) 
                    }
                    val currentVal = categoryInputs[cat.key] ?: ""

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MontraSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(cat.pastelBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.displayName,
                                    tint = cat.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cat.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MontraTextPrimary
                                )
                                if (status != null && status.currentSpent > 0) {
                                    Text(
                                        text = "Spent ${FormatUtils.formatCurrency(status.currentSpent, currency)}",
                                        fontSize = 11.sp,
                                        color = MontraTextSecondary
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { categoryInputs[cat.key] = AmountInputUtils.sanitizeAmount(it) },
                                modifier = Modifier
                                    .width(115.dp)
                                    .testTag("budget_input_${cat.key}"),
                                prefix = { Text(currency.symbol, color = MontraTextSecondary, fontSize = 12.sp) },
                                placeholder = { Text("No limit", color = MontraTextMuted, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = MontraBorder,
                                    focusedTextColor = MontraTextPrimary,
                                    unfocusedTextColor = MontraTextPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val overallVal = overallInput.toDoubleOrNull() ?: 1500.0
                    onSaveBudget("OVERALL", overallVal)
                    allCategories.forEach { cat ->
                        val catVal = categoryInputs[cat.key]?.toDoubleOrNull()
                        if (catVal != null && catVal > 0) {
                            onSaveBudget(cat.key, catVal)
                        } else if (categoryInputs[cat.key]?.isBlank() == true) {
                            // If user explicitly blanked it out, delete the budget for that category
                            onDeleteBudget(cat.key)
                        }
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_budgets_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All Budgets", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
