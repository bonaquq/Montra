package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActionModal(
    onManualEntry: () -> Unit,
    onAutoScan: () -> Unit,
    onManageBudgets: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "New Entry",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose how you would like to track this transaction",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            ActionOptionRow(
                title = "Manual Expense Entry",
                subtitle = "Add amount, category, custom notes & receipt photo",
                icon = Icons.Filled.Add,
                iconBg = Color(0xFFEDE9FE),
                iconTint = PurplePrimary,
                onClick = {
                    onDismiss()
                    onManualEntry()
                },
                tag = "modal_manual_entry"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionOptionRow(
                title = "Auto-Scan Bank SMS or Receipt",
                subtitle = "Instant smart parsing from message text or camera",
                icon = Icons.Filled.AutoAwesome,
                iconBg = Color(0xFFFEF3C7),
                iconTint = Color(0xFFD97706),
                onClick = {
                    onDismiss()
                    onAutoScan()
                },
                tag = "modal_auto_scan"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionOptionRow(
                title = "Adjust Budget or Goal",
                subtitle = "Set monthly target limits per category",
                icon = Icons.Filled.TrackChanges,
                iconBg = Color(0xFFE0F2FE),
                iconTint = Color(0xFF0284C7),
                onClick = {
                    onDismiss()
                    onManageBudgets()
                },
                tag = "modal_adjust_budget"
            )
        }
    }
}

@Composable
private fun ActionOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F9FD)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                )
            }
        }
    }
}
