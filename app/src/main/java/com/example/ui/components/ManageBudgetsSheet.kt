package com.example.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.Budget
import com.example.data.ExpenseCategory
import com.example.data.SupportedCurrency
import com.example.ui.BudgetStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBudgetsSheet(
    budgetStatuses: List<BudgetStatus>,
    currency: SupportedCurrency,
    onSaveBudget: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Pre-populate category targets
    val overallLimit = budgetStatuses.firstOrNull { it.categoryName == "OVERALL" }?.monthlyLimit ?: 800.0
    var overallInput by remember { mutableStateOf(String.format("%.0f", overallLimit)) }

    val categoryInputs = remember {
        mutableStateMapOf<String, String>().apply {
            ExpenseCategory.entries.filter { !it.isIncomeCategory }.forEach { cat ->
                val current = budgetStatuses.firstOrNull { it.categoryName == cat.name }?.monthlyLimit
                put(cat.name, if (current != null && current > 0) String.format("%.0f", current) else "")
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("manage_budgets_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Flexible Monthly Budgets",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Set overall caps and category allowances (${currency.code})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_budgets_sheet_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Overall Budget Setting
            Text(
                text = "Overall Monthly Cap",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = overallInput,
                onValueChange = { overallInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_overall_input"),
                prefix = { Text("${currency.symbol} ") },
                label = { Text("Total Monthly Limit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Category Allowances (Optional)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ExpenseCategory.entries.filter { !it.isIncomeCategory }.forEach { cat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cat.color.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.displayName,
                                    tint = cat.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = categoryInputs[cat.name] ?: "",
                            onValueChange = { categoryInputs[cat.name] = it },
                            modifier = Modifier
                                .width(120.dp)
                                .testTag("budget_input_${cat.name}"),
                            prefix = { Text(currency.symbol) },
                            placeholder = { Text("Limit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val overallVal = overallInput.toDoubleOrNull() ?: 800.0
                    onSaveBudget("OVERALL", overallVal)
                    ExpenseCategory.entries.filter { !it.isIncomeCategory }.forEach { cat ->
                        val catVal = categoryInputs[cat.name]?.toDoubleOrNull()
                        if (catVal != null && catVal > 0) {
                            onSaveBudget(cat.name, catVal)
                        }
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_budgets_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Budgets")
            }
        }
    }
}
