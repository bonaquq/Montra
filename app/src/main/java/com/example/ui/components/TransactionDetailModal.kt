package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CategoryRegistry
import com.example.data.Expense
import com.example.data.ExpenseCategory
import com.example.data.SupportedCurrency
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraExpenseRed
import com.example.ui.theme.MontraIncomeGreen
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import com.example.util.AmountInputUtils
import com.example.util.DateUtils
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailModal(
    expense: Expense,
    currency: SupportedCurrency,
    onDismiss: () -> Unit,
    onUpdateExpense: (
        id: Long,
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        note: String,
        currencyCode: String,
        receiptUri: String?,
        isIncome: Boolean
    ) -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val catInfo = remember(expense.category) {
        CategoryRegistry.getCategoryItem(expense.category)
    }

    // Edit form fields
    var editTitle by remember(expense) { mutableStateOf(expense.title) }
    var editAmountText by remember(expense) {
        mutableStateOf(String.format(Locale.US, "%.2f", expense.amount))
    }
    var editCategoryKey by remember(expense) { mutableStateOf(expense.category) }
    var editDateMillis by remember(expense) { mutableLongStateOf(expense.dateMillis) }
    var editNote by remember(expense) { mutableStateOf(expense.note) }
    var editIsIncome by remember(expense) { mutableStateOf(expense.isIncome) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = editDateMillis)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MontraSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("transaction_detail_modal")
        ) {
            if (!isEditing) {
                // View Mode
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transaction Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_detail_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MontraTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Category Icon Circle with category tint
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(catInfo.pastelBg)
                            .border(1.5.dp, catInfo.color.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = catInfo.icon,
                            contentDescription = catInfo.displayName,
                            tint = catInfo.color,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = expense.title.ifEmpty { catInfo.displayName },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Amount Display
                    val sign = if (expense.isIncome) "+" else "-"
                    val displayAmount = FormatUtils.formatCurrency(expense.amount, currency)
                    Text(
                        text = "$sign$displayAmount",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (expense.isIncome) MontraIncomeGreen else MontraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Metadata Details Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MontraSurfaceElevated)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Type Row
                        DetailInfoRow(
                            label = "Type",
                            value = if (expense.isIncome) "Income" else "Expense",
                            badgeColor = if (expense.isIncome) MontraIncomeGreen else MontraExpenseRed
                        )

                        // Category Row
                        DetailInfoRow(
                            label = "Category",
                            value = catInfo.displayName,
                            badgeColor = catInfo.color
                        )

                        // Date Row
                        DetailInfoRow(
                            label = "Date & Time",
                            value = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(expense.dateMillis))
                        )

                        // Currency Row
                        DetailInfoRow(
                            label = "Currency",
                            value = "${expense.currency.code} (${expense.currency.symbol})"
                        )

                        // Note Row
                        if (expense.note.isNotBlank()) {
                            DetailInfoRow(
                                label = "Note",
                                value = expense.note
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons (Edit & Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Delete Button
                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("delete_transaction_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MontraExpenseRed.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MontraExpenseRed
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Edit Button
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("edit_transaction_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MontraTextPrimary,
                                contentColor = MontraBackground
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Edit Form Mode
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Edit Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Transaction",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                        IconButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cancel Edit",
                                tint = MontraTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Income / Expense Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MontraSurfaceElevated)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!editIsIncome) MontraExpenseRed.copy(alpha = 0.85f) else Color.Transparent)
                                .clickable { editIsIncome = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = MontraTextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Expense",
                                    fontSize = 13.sp,
                                    fontWeight = if (!editIsIncome) FontWeight.Bold else FontWeight.Medium,
                                    color = MontraTextPrimary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (editIsIncome) MontraIncomeGreen.copy(alpha = 0.85f) else Color.Transparent)
                                .clickable { editIsIncome = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = null,
                                    tint = MontraTextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Income",
                                    fontSize = 13.sp,
                                    fontWeight = if (editIsIncome) FontWeight.Bold else FontWeight.Medium,
                                    color = MontraTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Field
                    Text(
                        text = "Amount (${currency.symbol})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = {
                            editAmountText = AmountInputUtils.sanitizeAmount(it)
                            editError = null
                        },
                        placeholder = { Text("0.00", color = MontraTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MontraTextPrimary,
                            unfocusedBorderColor = MontraBorder,
                            focusedTextColor = MontraTextPrimary,
                            unfocusedTextColor = MontraTextPrimary,
                            focusedContainerColor = MontraSurfaceElevated,
                            unfocusedContainerColor = MontraSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_amount_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title / Merchant Field
                    Text(
                        text = "Title / Merchant",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = {
                            editTitle = it
                            editError = null
                        },
                        placeholder = { Text("e.g. Grocery Store, Coffee", color = MontraTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MontraTextPrimary,
                            unfocusedBorderColor = MontraBorder,
                            focusedTextColor = MontraTextPrimary,
                            unfocusedTextColor = MontraTextPrimary,
                            focusedContainerColor = MontraSurfaceElevated,
                            unfocusedContainerColor = MontraSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_title_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selector
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Chips (Horizontal Scroll or Compact Grid)
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categoriesToDisplay = if (expense.isIncome) {
                            listOf(CategoryRegistry.getIncomeCategoryItem())
                        } else {
                            CategoryRegistry.getExpenseCategoryItems()
                        }
                        items(categoriesToDisplay) { cat ->
                            val isSelected = editCategoryKey.equals(cat.name, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) cat.color.copy(alpha = 0.25f)
                                        else MontraSurfaceElevated
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) cat.color else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { editCategoryKey = cat.name }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.displayName,
                                        tint = if (isSelected) cat.color else MontraTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = cat.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MontraTextPrimary else MontraTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date Selector
                    Text(
                        text = "Date",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MontraSurfaceElevated)
                            .clickable { isDatePickerOpen = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Select Date",
                                tint = MontraTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = DateUtils.formatDisplayDate(editDateMillis),
                                fontSize = 14.sp,
                                color = MontraTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Change",
                            fontSize = 12.sp,
                            color = MontraTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Note Field
                    Text(
                        text = "Note (Optional)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        placeholder = { Text("Add any extra details...", color = MontraTextMuted) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MontraTextPrimary,
                            unfocusedBorderColor = MontraBorder,
                            focusedTextColor = MontraTextPrimary,
                            unfocusedTextColor = MontraTextPrimary,
                            focusedContainerColor = MontraSurfaceElevated,
                            unfocusedContainerColor = MontraSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_note_input")
                    )

                    // Error text if validation fails
                    if (editError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = editError ?: "",
                            fontSize = 12.sp,
                            color = MontraExpenseRed,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Action Buttons (Cancel / Save Changes)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MontraTextSecondary
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                val parsed = editAmountText.toDoubleOrNull()
                                if (parsed == null || parsed <= 0.0) {
                                    editError = "Please enter a valid amount greater than 0"
                                    return@Button
                                }
                                if (editTitle.isBlank()) {
                                    editTitle = CategoryRegistry.getCategoryItem(editCategoryKey).displayName
                                }
                                onUpdateExpense(
                                    expense.id,
                                    editTitle.trim(),
                                    parsed,
                                    editCategoryKey,
                                    editDateMillis,
                                    editNote.trim(),
                                    expense.currencyCode,
                                    expense.receiptUri,
                                    editIsIncome
                                )
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_changes_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MontraTextPrimary,
                                contentColor = MontraBackground
                            )
                        ) {
                            Text(
                                text = "Save Changes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // DatePicker Dialog for Editing
    if (isDatePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        editDateMillis = it
                    }
                    isDatePickerOpen = false
                }) {
                    Text("OK", color = MontraTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("Cancel", color = MontraTextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Transaction?",
                    fontWeight = FontWeight.Bold,
                    color = MontraTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${expense.title.ifEmpty { expense.expenseCategory.displayName }}\"? This action cannot be undone.",
                    color = MontraTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteExpense(expense)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MontraExpenseRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = MontraTextSecondary)
                }
            },
            containerColor = MontraSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    badgeColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MontraTextSecondary,
            fontWeight = FontWeight.Medium
        )
        if (badgeColor != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    color = badgeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 13.sp,
                color = MontraTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
