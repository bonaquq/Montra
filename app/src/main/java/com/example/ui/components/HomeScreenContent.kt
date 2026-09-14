package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.data.SupportedCurrency
import com.example.ui.BudgetPeriod
import com.example.ui.ExpenseUiState
import com.example.ui.ForeignCurrencyRecord
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraIncomeGreen
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import com.example.util.DateUtils
import com.example.util.FormatUtils
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import java.util.Locale

@Composable
fun HomeScreenContent(
    uiState: ExpenseUiState,
    onOpenSettings: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onSeeAllTransactionsWithFilter: (String) -> Unit = {},
    onManageBudgets: () -> Unit = {},
    onToggleBudgetPeriod: (BudgetPeriod) -> Unit = {},
    onToggleBalanceVisibility: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val overallBudget = remember(uiState.budgetStatuses) {
        uiState.budgetStatuses.firstOrNull { it.categoryName.equals("OVERALL", ignoreCase = true) }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MontraBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 90.dp)
        ) {
            // Header: Top Bar
            item {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Montra",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontraTextPrimary,
                                    modifier = Modifier.testTag("txt_home_title")
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2563EB))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("badge_beta"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "beta",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Track your spending. Build your goals.",
                                fontSize = 13.sp,
                                color = MontraTextSecondary
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MontraSurface)
                                .testTag("btn_home_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = MontraTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Total Balance Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSeeAllTransactionsWithFilter("ALL") }
                        .testTag("card_total_balance"),
                    color = MontraSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Top row: Label and Eye icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Balance",
                                fontSize = 13.sp,
                                color = MontraTextSecondary,
                                fontWeight = FontWeight.Medium
                            )

                            IconButton(
                                onClick = onToggleBalanceVisibility,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("btn_toggle_balance_visibility")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isBalanceHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle Balance Visibility",
                                    tint = MontraTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Large Balance text
                        val balanceFormatted = if (uiState.isBalanceHidden) {
                            "••••••••"
                        } else {
                            FormatUtils.formatCurrency(uiState.totalBalance, uiState.selectedCurrency)
                        }

                        Text(
                            text = balanceFormatted,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary,
                            modifier = Modifier.testTag("txt_total_balance_value")
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(thickness = 1.dp, color = MontraBorder)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom row: Total Spent & Total Income
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Total Spent
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSeeAllTransactionsWithFilter("EXPENSES") }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Total Spent",
                                    fontSize = 12.sp,
                                    color = MontraTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.isBalanceHidden) "••••" else FormatUtils.formatCurrency(uiState.totalSpent, uiState.selectedCurrency),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MontraTextPrimary,
                                    modifier = Modifier.testTag("txt_total_spent_value")
                                )
                            }

                            // Right: Total Income
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSeeAllTransactionsWithFilter("INCOME") }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Total Income",
                                    fontSize = 12.sp,
                                    color = MontraTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.isBalanceHidden) "••••" else FormatUtils.formatCurrency(uiState.totalIncome, uiState.selectedCurrency),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MontraIncomeGreen,
                                    modifier = Modifier.testTag("txt_total_income_value")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Overall Monthly / Daily Budget Summary Card
            if (overallBudget != null) {
                item {
                    val isDaily = uiState.overallBudgetPeriod == BudgetPeriod.DAILY
                    val isExceeded = if (isDaily) {
                        overallBudget.todaySpent > overallBudget.dailyLimit && overallBudget.dailyLimit > 0
                    } else {
                        overallBudget.currentSpent > overallBudget.monthlyLimit && overallBudget.monthlyLimit > 0
                    }
                    val progressFraction = if (isDaily) {
                        if (overallBudget.dailyLimit > 0) {
                            (overallBudget.todaySpent / overallBudget.dailyLimit).toFloat().coerceIn(0f, 1f)
                        } else 0f
                    } else {
                        if (overallBudget.monthlyLimit > 0) {
                            (overallBudget.currentSpent / overallBudget.monthlyLimit).toFloat().coerceIn(0f, 1f)
                        } else 0f
                    }
                    val progressColor = if (isExceeded) Color(0xFFEF4444) else if (progressFraction > 0.8f) Color(0xFFF59E0B) else Color(0xFF6366F1)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onManageBudgets() }
                            .testTag("card_overall_budget_summary"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MontraSurface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            if (isExceeded) Color(0xFFEF4444).copy(alpha = 0.5f) else MontraBorder
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = progressColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isDaily) "Overall Daily Budget" else "Overall Monthly Budget",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraTextPrimary,
                                        modifier = Modifier.testTag("txt_overall_budget_title")
                                    )
                                }

                                // Monthly / Daily Segmented Toggle
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MontraBackground)
                                        .padding(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (!isDaily) Color(0xFF6366F1) else Color.Transparent)
                                            .clickable { onToggleBudgetPeriod(BudgetPeriod.MONTHLY) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("btn_budget_period_monthly"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Monthly",
                                            fontSize = 11.sp,
                                            fontWeight = if (!isDaily) FontWeight.Bold else FontWeight.Medium,
                                            color = if (!isDaily) Color.White else MontraTextSecondary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isDaily) Color(0xFF6366F1) else Color.Transparent)
                                            .clickable { onToggleBudgetPeriod(BudgetPeriod.DAILY) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("btn_budget_period_daily"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Daily",
                                            fontSize = 11.sp,
                                            fontWeight = if (isDaily) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isDaily) Color.White else MontraTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = if (isDaily) "Spent today" else "Spent this month",
                                        fontSize = 11.sp,
                                        color = MontraTextSecondary
                                    )
                                    Text(
                                        text = FormatUtils.formatCurrency(
                                            if (isDaily) overallBudget.todaySpent else overallBudget.currentSpent, 
                                            uiState.selectedCurrency
                                        ),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraTextPrimary,
                                        modifier = Modifier.testTag("txt_overall_budget_spent")
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isDaily) "Daily limit (${uiState.daysInCurrentMonth}d avg)" else "Monthly limit",
                                        fontSize = 11.sp,
                                        color = MontraTextSecondary
                                    )
                                    Text(
                                        text = FormatUtils.formatCurrency(
                                            if (isDaily) overallBudget.dailyLimit else overallBudget.monthlyLimit, 
                                            uiState.selectedCurrency
                                        ),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MontraTextSecondary,
                                        modifier = Modifier.testTag("txt_overall_budget_limit")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress bar
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("indicator_overall_budget_progress"),
                                color = progressColor,
                                trackColor = MontraBackground
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val pctUsed = if (isDaily) {
                                    if (overallBudget.dailyLimit > 0) (overallBudget.todaySpent / overallBudget.dailyLimit * 100).toInt() else 0
                                } else {
                                    if (overallBudget.monthlyLimit > 0) (overallBudget.currentSpent / overallBudget.monthlyLimit * 100).toInt() else 0
                                }
                                Text(
                                    text = if (isDaily) "$pctUsed% used today" else "$pctUsed% used",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isExceeded) Color(0xFFEF4444) else MontraTextSecondary,
                                    modifier = Modifier.testTag("txt_overall_budget_pct")
                                )
                                Text(
                                    text = if (isDaily) {
                                        if (isExceeded) {
                                            "Over by ${FormatUtils.formatCurrency(overallBudget.todaySpent - overallBudget.dailyLimit, uiState.selectedCurrency)}"
                                        } else {
                                            "${FormatUtils.formatCurrency(overallBudget.dailyRemaining, uiState.selectedCurrency)} remaining today"
                                        }
                                    } else {
                                        if (isExceeded) {
                                            "Over by ${FormatUtils.formatCurrency(overallBudget.currentSpent - overallBudget.monthlyLimit, uiState.selectedCurrency)}"
                                        } else {
                                            "${FormatUtils.formatCurrency(overallBudget.remaining, uiState.selectedCurrency)} remaining"
                                        }
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isExceeded) Color(0xFFEF4444) else MontraTextSecondary,
                                    modifier = Modifier.testTag("txt_overall_budget_remaining")
                                )
                            }
                        }
                    }
                }
            }

            // Separate Foreign Balances Card - Compact & visible only when foreign currency records exist
            if (uiState.foreignCurrencyRecords.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .testTag("card_foreign_currency_records"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MontraSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Header Row: Title on Left, Total Portfolio Converted Value on Right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Language,
                                        contentDescription = "Foreign Balances",
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Foreign Balances",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraTextPrimary
                                    )
                                }

                                Text(
                                    text = if (uiState.isBalanceHidden) "••••" else "Total ≈ ${FormatUtils.formatCurrency(
                                        uiState.totalForeignHoldingsInMainCurrency,
                                        uiState.selectedCurrency
                                    )}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF60A5FA),
                                    modifier = Modifier.testTag("txt_total_foreign_holdings")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Compact Horizontal Scroll of Foreign Currency Cards
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.foreignCurrencyRecords.forEach { record ->
                                    val flag = when (record.currency) {
                                        SupportedCurrency.USD -> "🇺🇸"
                                        SupportedCurrency.EUR -> "🇪🇺"
                                        SupportedCurrency.MVR -> "🇲🇻"
                                        SupportedCurrency.GBP -> "🇬🇧"
                                        SupportedCurrency.JPY -> "🇯🇵"
                                        SupportedCurrency.INR -> "🇮🇳"
                                        SupportedCurrency.CAD -> "🇨🇦"
                                        SupportedCurrency.AUD -> "🇦🇺"
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onSeeAllTransactionsWithFilter("ALL") }
                                            .testTag("card_foreign_record_${record.currency.code.lowercase()}"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MontraSurfaceElevated,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(text = flag, fontSize = 13.sp)
                                                Text(
                                                    text = record.currency.code,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MontraTextSecondary
                                                )
                                            }

                                            Text(
                                                text = if (uiState.isBalanceHidden) "••••" else FormatUtils.formatCurrency(
                                                    record.netAmount,
                                                    record.currency
                                                ),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (record.netAmount >= 0) MontraIncomeGreen else Color(0xFFEF4444),
                                                maxLines = 1
                                            )

                                            Text(
                                                text = if (uiState.isBalanceHidden) "••••" else "≈ ${FormatUtils.formatCurrency(record.convertedToMain, uiState.selectedCurrency)}",
                                                fontSize = 10.sp,
                                                color = MontraTextMuted,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Budget Alerts Banner if any active
            if (uiState.budgetAlerts.isNotEmpty()) {
                val primaryAlert = uiState.budgetAlerts.first()
                item {
                    val alertColor = if (primaryAlert.isExceeded) androidx.compose.ui.graphics.Color(0xFFEF4444) else androidx.compose.ui.graphics.Color(0xFFF59E0B)
                    val alertBg = if (primaryAlert.isExceeded) androidx.compose.ui.graphics.Color(0xFF2C1518) else androidx.compose.ui.graphics.Color(0xFF2B2113)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = alertBg,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, alertColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (primaryAlert.isExceeded) Icons.Filled.Warning else Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = alertColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Budget Alert: ${primaryAlert.displayName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontraTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = primaryAlert.message,
                                    fontSize = 12.sp,
                                    color = MontraTextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section Header: Recent Transactions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MontraTextPrimary
                    )

                    Text(
                        text = "See all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MontraTextSecondary,
                        modifier = Modifier
                            .testTag("btn_see_all_transactions")
                            .clickable(onClick = onSeeAllTransactions)
                            .padding(4.dp)
                    )
                }
            }

            // Recent Transactions List
            val recentList = uiState.allExpensesUnfiltered.take(6)
            if (recentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent transactions",
                            fontSize = 14.sp,
                            color = MontraTextMuted
                        )
                    }
                }
            } else {
                items(recentList) { expense ->
                    MontraTransactionRow(
                        expense = expense,
                        currency = uiState.selectedCurrency,
                        onClick = { onExpenseClick(expense) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Floating Action Button (+) on bottom right
        FloatingActionButton(
            onClick = onOpenAddExpense,
            containerColor = MontraButtonBg,
            contentColor = MontraTextPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(54.dp)
                .testTag("fab_add_expense")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Expense",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MontraTransactionRow(
    expense: Expense,
    onClick: () -> Unit,
    currency: SupportedCurrency = SupportedCurrency.USD,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MontraSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("transaction_row_${expense.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Circular Icon badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MontraSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = expense.expenseCategory.icon,
                    contentDescription = expense.expenseCategory.displayName,
                    tint = MontraTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = expense.title.ifEmpty { expense.expenseCategory.displayName },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MontraTextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = DateUtils.formatDisplayDate(expense.dateMillis),
                        fontSize = 12.sp,
                        color = MontraTextSecondary
                    )
                    if (expense.currency != currency) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = expense.currency.code,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA)
                            )
                        }
                    }
                }
            }
        }

        // Amount on right
        val isIncome = expense.isIncome
        val sign = if (isIncome) "+" else "-"
        val formattedAmount = FormatUtils.formatCurrency(expense.amount, expense.currency)

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$sign$formattedAmount",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIncome) MontraIncomeGreen else MontraTextPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Transaction options",
                    tint = MontraTextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
            if (expense.currency != currency) {
                val convertedVal = SupportedCurrency.convert(expense.amount, expense.currency, currency)
                Text(
                    text = "≈ $sign${FormatUtils.formatCurrency(convertedVal, currency)}",
                    fontSize = 11.sp,
                    color = MontraTextMuted
                )
            }
        }
    }
}
