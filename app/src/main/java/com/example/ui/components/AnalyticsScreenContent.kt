package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ExpenseCategory
import com.example.ui.ExpenseUiState
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraIncomeGreen
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreenContent(
    uiState: ExpenseUiState,
    onPeriodChanged: (String) -> Unit,
    onNavigateDate: (Int) -> Unit,
    onSetCustomDateRange: (Long, Long) -> Unit,
    onDismissBudgetAlert: (String) -> Unit,
    onSetBudgetThreshold: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDateDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MontraBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 90.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analytics & Insights",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MontraTextPrimary,
                    modifier = Modifier.testTag("txt_analytics_title")
                )

                // Health score badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MontraSurface)
                        .border(1.dp, MontraBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Score: ${uiState.financialHealthScore}/100",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MontraTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Period Filter Segment: Weekly | Monthly | Yearly | Custom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MontraSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val periods = listOf(
                    "WEEKLY" to "Weekly",
                    "MONTHLY" to "Monthly",
                    "YEARLY" to "Yearly",
                    "CUSTOM" to "Custom"
                )
                periods.forEach { (key, label) ->
                    val isSelected = uiState.analyticsPeriod.equals(key, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MontraButtonBg else MontraSurface)
                            .clickable {
                                if (key == "CUSTOM") {
                                    showCustomDateDialog = true
                                } else {
                                    onPeriodChanged(key)
                                }
                            }
                            .padding(vertical = 9.dp)
                            .testTag("analytics_period_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) MontraTextPrimary else MontraTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date Range Navigation Row: < Specific Date Range >
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MontraSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigateDate(-1) },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("btn_analytics_prev_date")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Previous Date Range",
                        tint = MontraTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCustomDateDialog = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("btn_select_specific_date_range"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Pick Date",
                        tint = MontraTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = uiState.analyticsDateRangeLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MontraTextPrimary,
                        modifier = Modifier.testTag("txt_analytics_date_range_label")
                    )
                }

                IconButton(
                    onClick = { onNavigateDate(1) },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("btn_analytics_next_date")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Next Date Range",
                        tint = MontraTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Budget Alerts Section (if any alerts active)
        if (uiState.budgetAlerts.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
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
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Budget Limits & Alerts (${uiState.budgetAlerts.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontraTextPrimary
                            )
                        }

                        Text(
                            text = "Threshold: ${uiState.budgetWarningThresholdPercent}%",
                            fontSize = 12.sp,
                            color = MontraTextSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showThresholdDialog = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.budgetAlerts.forEach { alert ->
                        val alertColor = if (alert.isExceeded) Color(0xFFEF4444) else Color(0xFFF59E0B)
                        val alertBg = if (alert.isExceeded) Color(0xFF2E1517) else Color(0xFF2B2113)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            color = alertBg,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, alertColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (alert.isExceeded) Icons.Filled.Warning else Icons.Filled.NotificationsActive,
                                        contentDescription = null,
                                        tint = alertColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = alert.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontraTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = alert.message,
                                            fontSize = 12.sp,
                                            color = MontraTextSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDismissBudgetAlert(alert.categoryName) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss",
                                        tint = MontraTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Spending Bar Chart Container (Dynamic according to selected dates)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = MontraSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Total Spent in Period",
                        fontSize = 13.sp,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatCurrency(uiState.analyticsTotalSpent, uiState.selectedCurrency),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextPrimary,
                        modifier = Modifier.testTag("txt_analytics_total_spent")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val dailyBars = uiState.analyticsDailyBars
                    val maxVal = (dailyBars.maxOfOrNull { it.amount } ?: 1f).coerceAtLeast(10f)

                    if (dailyBars.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expense data for this selected timeframe",
                                fontSize = 13.sp,
                                color = MontraTextMuted
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Y-Axis labels
                            Column(
                                modifier = Modifier
                                    .height(130.dp)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    FormatUtils.formatCurrency(maxVal.toDouble(), uiState.selectedCurrency),
                                    fontSize = 9.sp,
                                    color = MontraTextMuted
                                )
                                Text(
                                    FormatUtils.formatCurrency((maxVal / 2).toDouble(), uiState.selectedCurrency),
                                    fontSize = 9.sp,
                                    color = MontraTextMuted
                                )
                                Text(
                                    FormatUtils.formatCurrency(0.0, uiState.selectedCurrency),
                                    fontSize = 9.sp,
                                    color = MontraTextMuted
                                )
                            }

                            // Dynamic Vertical Bars
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(160.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                dailyBars.forEach { bar ->
                                    val barHeightFraction = (bar.amount / maxVal).coerceIn(0.08f, 1f)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (bar.isHighest && bar.amount > 0f) {
                                            Text(
                                                text = FormatUtils.formatCurrency(bar.amount.toDouble(), uiState.selectedCurrency),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MontraIncomeGreen,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(if (dailyBars.size > 7) 12.dp else 18.dp)
                                                .height((110 * barHeightFraction).dp)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(
                                                    if (bar.isHighest) MontraIncomeGreen else Color(0xFF4B5563)
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = bar.label,
                                            fontSize = if (dailyBars.size > 7) 9.sp else 11.sp,
                                            color = if (bar.isHighest) MontraTextPrimary else MontraTextMuted,
                                            fontWeight = if (bar.isHighest) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Top Categories Container (Donut Chart + Legend for this specific date range)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = MontraSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Category Breakdown",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MontraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val catSummaries = uiState.analyticsCategorySummaries
                    if (catSummaries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No category spending for this period",
                                fontSize = 14.sp,
                                color = MontraTextMuted
                            )
                        }
                    } else {
                        val palette = listOf(
                            Color(0xFF3B82F6),
                            Color(0xFF10B981),
                            Color(0xFFF59E0B),
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899),
                            Color(0xFF06B6D4),
                            Color(0xFF6B7280)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Donut Chart
                            Box(
                                modifier = Modifier.size(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(110.dp)) {
                                    var startAngle = -90f
                                    catSummaries.forEachIndexed { idx, summary ->
                                        val color = palette[idx % palette.size]
                                        val sweep = summary.percentage * 360f
                                        if (sweep > 0f) {
                                            drawArc(
                                                color = color,
                                                startAngle = startAngle,
                                                sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                                                useCenter = false,
                                                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                            startAngle += sweep
                                        }
                                    }
                                }

                                Text(
                                    text = "${catSummaries.size} Cat",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontraTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Category breakdown legend
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                catSummaries.take(5).forEachIndexed { idx, summary ->
                                    val color = palette[idx % palette.size]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text(
                                                text = summary.displayName,
                                                fontSize = 13.sp,
                                                color = MontraTextSecondary,
                                                maxLines = 1
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = String.format(Locale.US, "%.1f%%", summary.percentage * 100f),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MontraTextPrimary
                                            )
                                            Text(
                                                text = FormatUtils.formatCurrency(summary.totalAmount, uiState.selectedCurrency),
                                                fontSize = 10.sp,
                                                color = MontraTextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // AI Insights & Anomaly Detection Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = MontraSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
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
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "AI Insights & Anomalies",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MontraTextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E2640))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Gemini Powered",
                                fontSize = 11.sp,
                                color = Color(0xFFA78BFA),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uiState.aiInsights.isEmpty()) {
                        Text(
                            text = "No unusual spending anomalies or duplicate charges detected.",
                            fontSize = 13.sp,
                            color = MontraTextMuted
                        )
                    } else {
                        uiState.aiInsights.forEach { insight ->
                            val iconColor = when (insight.severity) {
                                "ALERT" -> Color(0xFFEF4444)
                                "WARNING" -> Color(0xFFF59E0B)
                                else -> Color(0xFF38BDF8)
                            }
                            val cardBg = when (insight.severity) {
                                "ALERT" -> Color(0xFF2C1518)
                                "WARNING" -> Color(0xFF2A2113)
                                else -> MontraSurfaceElevated
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                color = cardBg,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (insight.type) {
                                                "DUPLICATE" -> Icons.Filled.Warning
                                                "ANOMALY" -> Icons.Filled.NotificationsActive
                                                else -> Icons.Filled.AutoAwesome
                                            },
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = insight.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontraTextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = insight.description,
                                        fontSize = 12.sp,
                                        color = MontraTextSecondary,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Date Range Picker Dialog
    if (showCustomDateDialog) {
        CustomDateRangeModal(
            currentStart = uiState.analyticsStartDateMillis,
            currentEnd = uiState.analyticsEndDateMillis,
            onDismiss = { showCustomDateDialog = false },
            onApply = { start, end ->
                onSetCustomDateRange(start, end)
                showCustomDateDialog = false
            }
        )
    }

    // Budget Threshold Slider Dialog
    if (showThresholdDialog) {
        Dialog(onDismissRequest = { showThresholdDialog = false }) {
            var tempThreshold by remember { mutableStateOf(uiState.budgetWarningThresholdPercent.toFloat()) }
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(24.dp)),
                color = MontraSurface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Budget Alert Threshold",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notify when category spending reaches ${tempThreshold.toInt()}% of its limit.",
                        fontSize = 13.sp,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Slider(
                        value = tempThreshold,
                        onValueChange = { tempThreshold = it },
                        valueRange = 50f..95f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MontraButtonBg,
                            activeTrackColor = MontraButtonBg,
                            inactiveTrackColor = MontraSurfaceElevated
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showThresholdDialog = false }) {
                            Text("Cancel", color = MontraTextMuted)
                        }
                        Button(
                            onClick = {
                                onSetBudgetThreshold(tempThreshold.toInt())
                                showThresholdDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MontraButtonBg)
                        ) {
                            Text("Save", color = MontraTextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomDateRangeModal(
    currentStart: Long,
    currentEnd: Long,
    onDismiss: () -> Unit,
    onApply: (Long, Long) -> Unit
) {
    var selectedPreset by remember { mutableStateOf("7DAYS") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MontraSurface,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Switch Date Range",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = MontraTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val presets = listOf(
                    "TODAY" to "Today",
                    "7DAYS" to "Past 7 Days",
                    "14DAYS" to "Past 14 Days",
                    "30DAYS" to "Past 30 Days",
                    "MONTH" to "This Month",
                    "LAST_MONTH" to "Last Month",
                    "90DAYS" to "Past 3 Months"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { (key, label) ->
                        val isSelected = selectedPreset == key
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MontraButtonBg else MontraSurfaceElevated)
                                .clickable { selectedPreset = key }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MontraTextPrimary else MontraTextSecondary
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MontraIncomeGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        val (s, e) = when (selectedPreset) {
                            "TODAY" -> {
                                val cal = java.util.Calendar.getInstance()
                                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                cal.set(java.util.Calendar.MINUTE, 0)
                                Pair(cal.timeInMillis, now)
                            }
                            "7DAYS" -> Pair(now - 7 * 86400000L, now)
                            "14DAYS" -> Pair(now - 14 * 86400000L, now)
                            "30DAYS" -> Pair(now - 30 * 86400000L, now)
                            "MONTH" -> {
                                val cal = java.util.Calendar.getInstance()
                                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                cal.set(java.util.Calendar.MINUTE, 0)
                                Pair(cal.timeInMillis, now)
                            }
                            "LAST_MONTH" -> {
                                val cal = java.util.Calendar.getInstance()
                                cal.add(java.util.Calendar.MONTH, -1)
                                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                                val sMillis = cal.timeInMillis
                                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                                Pair(sMillis, cal.timeInMillis)
                            }
                            "90DAYS" -> Pair(now - 90 * 86400000L, now)
                            else -> Pair(now - 7 * 86400000L, now)
                        }
                        onApply(s, e)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MontraButtonBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply Date Filter", color = MontraTextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
