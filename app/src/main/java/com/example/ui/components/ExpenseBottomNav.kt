package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary

@Composable
fun ExpenseBottomNav(
    activeTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MontraBackground)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MontraBorder
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MontraNavTabItem(
                label = "Home",
                isSelected = activeTab == AppTab.HOME,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onTabSelected(AppTab.HOME) },
                testTag = "nav_tab_home",
                modifier = Modifier.weight(1f)
            )

            MontraNavTabItem(
                label = "Transactions",
                isSelected = activeTab == AppTab.TRANSACTIONS,
                selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
                onClick = { onTabSelected(AppTab.TRANSACTIONS) },
                testTag = "nav_tab_transactions",
                modifier = Modifier.weight(1f)
            )

            MontraNavTabItem(
                label = "Analytics",
                isSelected = activeTab == AppTab.ANALYTICS,
                selectedIcon = Icons.Filled.BarChart,
                unselectedIcon = Icons.Outlined.BarChart,
                onClick = { onTabSelected(AppTab.ANALYTICS) },
                testTag = "nav_tab_analytics",
                modifier = Modifier.weight(1f)
            )

            MontraNavTabItem(
                label = "Categories",
                isSelected = activeTab == AppTab.CATEGORIES,
                selectedIcon = Icons.Filled.Category,
                unselectedIcon = Icons.Outlined.Category,
                onClick = { onTabSelected(AppTab.CATEGORIES) },
                testTag = "nav_tab_categories",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MontraNavTabItem(
    label: String,
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MontraTextPrimary else MontraTextMuted

    Box(
        modifier = modifier
            .testTag(testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
