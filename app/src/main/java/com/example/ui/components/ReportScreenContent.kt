package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseCategory
import com.example.ui.CategorySummary
import com.example.ui.ChartType
import com.example.ui.ExpenseUiState
import com.example.ui.ReportSubTab
import com.example.ui.theme.AppBackground
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun ReportScreenContent(
    uiState: ExpenseUiState,
    onBack: () -> Unit,
    onSubTabChanged: (ReportSubTab) -> Unit,
    onChartTypeChanged: (ChartType) -> Unit,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthMenu by remember { mutableStateOf(false) }
    val months = listOf("November 2025", "December 2025", "January 2026", "February 2026")

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(uiState.categorySummaries) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(700))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Top Bar: Back Arrow, "Report", Month Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Report",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                // Month Dropdown Pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showMonthMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedMonthLabel,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMonthMenu,
                        onDismissRequest = { showMonthMenu = false }
                    ) {
                        months.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    onMonthSelected(m)
                                    showMonthMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Segmented Switch: "Expenses" vs "Income"
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEFF1F8)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Expenses Tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSubTabChanged(ReportSubTab.EXPENSES) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.reportSubTab == ReportSubTab.EXPENSES) Color.White else Color.Transparent,
                            shadowElevation = if (uiState.reportSubTab == ReportSubTab.EXPENSES) 2.dp else 0.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Expenses",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = if (uiState.reportSubTab == ReportSubTab.EXPENSES) FontWeight.Bold else FontWeight.Medium,
                                        color = if (uiState.reportSubTab == ReportSubTab.EXPENSES) TextPrimary else TextSecondary
                                    )
                                )
                            }
                        }

                        // Income Tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSubTabChanged(ReportSubTab.INCOME) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.reportSubTab == ReportSubTab.INCOME) Color.White else Color.Transparent,
                            shadowElevation = if (uiState.reportSubTab == ReportSubTab.INCOME) 2.dp else 0.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Income",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = if (uiState.reportSubTab == ReportSubTab.INCOME) FontWeight.Bold else FontWeight.Medium,
                                        color = if (uiState.reportSubTab == ReportSubTab.INCOME) TextPrimary else TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Expenses Report & View Icons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.reportSubTab == ReportSubTab.EXPENSES) "Expenses Report" else "Income Report",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                // Donut vs Bar chart icon switch
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEFF1F8))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.chartType == ChartType.BAR) Color.White else Color.Transparent)
                            .clickable { onChartTypeChanged(ChartType.BAR) }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "Bar Chart",
                            tint = if (uiState.chartType == ChartType.BAR) TextPrimary else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.chartType == ChartType.DONUT) Color.White else Color.Transparent)
                            .clickable { onChartTypeChanged(ChartType.DONUT) }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = "Donut Chart",
                            tint = if (uiState.chartType == ChartType.DONUT) PurplePrimary else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Donut Chart Container
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val chartTotal = if (uiState.reportSubTab == ReportSubTab.EXPENSES) uiState.totalSpent else uiState.monthlyIncome
                val summaries = uiState.categorySummaries

                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(220.dp)) {
                        val strokeWidth = 32.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        var currentAngle = -90f

                        if (summaries.isNotEmpty()) {
                            summaries.forEach { item ->
                                val sweepAngle = (item.percentage * 360f * animationProgress.value)
                                if (sweepAngle > 1f) {
                                    drawArc(
                                        color = item.category.color,
                                        startAngle = currentAngle,
                                        sweepAngle = sweepAngle - 3f, // Small modern gap between segments
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    currentAngle += sweepAngle
                                }
                            }
                        } else {
                            // Fallback decorative ring
                            drawArc(
                                color = PurplePrimary.copy(alpha = 0.3f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }

                    // Center Labels: Total Expenses & Amount
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (uiState.reportSubTab == ReportSubTab.EXPENSES) "Total Expenses" else "Total Income",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtils.formatCurrency(chartTotal, uiState.selectedCurrency),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        )
                    }

                    // Interactive Tooltip Bubble indicator (like the black pill "31%" in reference)
                    val topPercent = summaries.firstOrNull()?.percentage?.let { (it * 100).toInt() } ?: 31
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 6.dp, y = (-12).dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PurplePrimary,
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "$topPercent%",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: All Expenses Breakdown List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Expenses",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "Total ${FormatUtils.formatCurrency(uiState.totalSpent, uiState.selectedCurrency)}",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
            }
        }

        // Category Breakdown Cards matching Mockup 2
        items(uiState.categorySummaries) { summary ->
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelected(summary.category) }
                        .testTag("report_category_${summary.category.name}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Category Icon with colored ring
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(summary.category.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = summary.category.icon,
                                        contentDescription = summary.category.displayName,
                                        tint = summary.category.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = summary.category.displayName,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${(summary.percentage * 100).toInt()}% of total",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 12.sp,
                                            color = TextMuted,
                                            fontWeight = FontWeight.Normal
                                        )
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatCurrency(summary.totalAmount, uiState.selectedCurrency),
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                // Trend badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "+12% vs last month",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress bar along the bottom of the card matching category color
                        LinearProgressIndicator(
                            progress = { summary.percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = summary.category.color,
                            trackColor = Color(0xFFEFF1F8)
                        )
                    }
                }
            }
        }
    }
}
