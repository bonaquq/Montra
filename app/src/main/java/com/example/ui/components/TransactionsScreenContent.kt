package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.ui.ExpenseUiState
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary

@Composable
fun TransactionsScreenContent(
    uiState: ExpenseUiState,
    onFilterChanged: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onExpenseClick: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MontraBackground)
            .statusBarsPadding()
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Transactions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MontraTextPrimary,
                modifier = Modifier.testTag("txt_transactions_title")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Segmented Filter Pill: All | Expenses | Income
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MontraSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filters = listOf("ALL" to "All", "EXPENSES" to "Expenses", "INCOME" to "Income")
                filters.forEach { (key, label) ->
                    val isSelected = uiState.transactionFilter.equals(key, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MontraButtonBg else MontraSurface)
                            .clickable { onFilterChanged(key) }
                            .padding(vertical = 10.dp)
                            .testTag("filter_tab_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) MontraTextPrimary else MontraTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MontraSurface)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MontraTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchChanged,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = MontraTextPrimary
                        ),
                        cursorBrush = SolidColor(MontraTextPrimary),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_search_transactions"),
                        decorationBox = { innerTextField ->
                            if (uiState.searchQuery.isEmpty()) {
                                Text(
                                    text = "Search merchant, note, or category...",
                                    fontSize = 14.sp,
                                    color = MontraTextMuted
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChanged("") },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("btn_clear_search")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear Search",
                                tint = MontraTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Transactions List
        val filteredList = uiState.expenses.filter { expense ->
            val matchesFilter = when (uiState.transactionFilter.uppercase()) {
                "EXPENSES" -> !expense.isIncome
                "INCOME" -> expense.isIncome
                else -> true
            }
            val matchesSearch = uiState.searchQuery.isBlank() ||
                    expense.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    expense.expenseCategory.displayName.contains(uiState.searchQuery, ignoreCase = true) ||
                    expense.note.contains(uiState.searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions found",
                    fontSize = 14.sp,
                    color = MontraTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { expense ->
                    MontraTransactionRow(
                        expense = expense,
                        currency = uiState.selectedCurrency,
                        onClick = { onExpenseClick(expense) }
                    )
                }
            }
        }
    }
}
