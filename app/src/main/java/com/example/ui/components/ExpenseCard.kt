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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryRegistry
import com.example.data.Expense
import com.example.data.ExpenseCategory
import com.example.data.SupportedCurrency
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun ExpenseCard(
    expense: Expense,
    appCurrency: SupportedCurrency,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val catItem = remember(expense.category) {
        CategoryRegistry.getCategoryItem(expense.category)
    }
    val pastelBg = catItem.pastelBg
    val iconTint = catItem.color

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_item_${expense.id}")
            .clickable { onEdit(expense) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon in pastel rounded squircle container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(pastelBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catItem.icon,
                    contentDescription = catItem.displayName,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details: Title and Tags
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tag badges row (like "Red Card", "Vacation", "Holdings" in reference)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tagText = if (expense.note.isNotBlank()) {
                        expense.note.take(16)
                    } else {
                        expense.expenseCategory.displayName
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF4F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SubdirectoryArrowRight,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = tagText,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (expense.isAutomated) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEDE9FE)
                        ) {
                            Text(
                                text = "Auto",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Negative Red/Coral Amount and Secondary Info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val formattedAmount = FormatUtils.formatCurrency(expense.amount, expense.currency)
                Text(
                    text = "-$formattedAmount",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444) // Coral/Red negative amount from reference mockup
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Date or converted amount
                val subtitleText = if (expense.currency != appCurrency) {
                    val converted = SupportedCurrency.convert(expense.amount, expense.currency, appCurrency)
                    "≈ ${FormatUtils.formatCurrency(converted, appCurrency)}"
                } else {
                    FormatUtils.formatDate(expense.dateMillis)
                }

                Text(
                    text = subtitleText,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            // Quick Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Options",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showMenu = false
                            onEdit(expense)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFEF4444)) },
                        leadingIcon = {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showMenu = false
                            onDelete(expense)
                        }
                    )
                }
            }
        }
    }
}
