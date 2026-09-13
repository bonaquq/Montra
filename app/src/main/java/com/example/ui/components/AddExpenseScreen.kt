package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CategoryRegistry
import com.example.data.ExpenseCategory
import com.example.data.SupportedCurrency
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
import com.example.util.ParsedReceiptData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    onAddExpense: (amount: Double, category: String, dateMillis: Long, description: String, isIncome: Boolean) -> Unit,
    onScanReceipt: ((Uri) -> Unit)? = null,
    isScanningReceipt: Boolean = false,
    scannedReceiptResult: ParsedReceiptData? = null,
    onClearScannedReceipt: (() -> Unit)? = null,
    selectedCurrency: SupportedCurrency = SupportedCurrency.USD,
    modifier: Modifier = Modifier
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryKey by remember { mutableStateOf(ExpenseCategory.FOOD.name) }
    val selectedCategoryItem = CategoryRegistry.getCategoryItem(selectedCategoryKey)
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var description by remember { mutableStateOf("") }
    var isCategoryPickerOpen by remember { mutableStateOf(false) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var isIncome by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scanNotice by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null && onScanReceipt != null) {
                onScanReceipt(uri)
            }
        }
    )

    LaunchedEffect(scannedReceiptResult) {
        scannedReceiptResult?.let { data ->
            if (data.amount > 0.0) {
                amountText = String.format(java.util.Locale.US, "%.2f", data.amount)
            }
            if (data.merchantOrTitle.isNotBlank()) {
                description = data.merchantOrTitle
            }
            if (data.dateMillis > 0L) {
                dateMillis = data.dateMillis
            }
            val resolvedItem = CategoryRegistry.getCategoryItem(data.categoryHint)
            selectedCategoryKey = resolvedItem.name
            scanNotice = "Auto-extracted: ${data.merchantOrTitle} (${data.categoryHint}) - ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", data.amount)}"
        }
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MontraBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_add_expense_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MontraTextPrimary
                )
            }

            Text(
                text = if (isIncome) "Add Income" else "Add Expense",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MontraTextPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        // Segmented Control Tabs for Expense vs Income
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MontraSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Expense Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isIncome) MontraSurfaceElevated else Color.Transparent)
                    .clickable {
                        if (isIncome) {
                            isIncome = false
                            if (selectedCategoryKey.equals(ExpenseCategory.INCOME.name, ignoreCase = true) ||
                                selectedCategoryKey.equals("SALARY", ignoreCase = true)
                            ) {
                                selectedCategoryKey = ExpenseCategory.FOOD.name
                            }
                        }
                    }
                    .padding(vertical = 10.dp)
                    .testTag("tab_switch_expense"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = "Expense",
                        tint = if (!isIncome) Color(0xFFF87171) else MontraTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Expense",
                        fontSize = 14.sp,
                        fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isIncome) MontraTextPrimary else MontraTextSecondary
                    )
                }
            }

            // Income Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isIncome) MontraIncomeGreen.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable {
                        if (!isIncome) {
                            isIncome = true
                            selectedCategoryKey = ExpenseCategory.INCOME.name
                        }
                    }
                    .padding(vertical = 10.dp)
                    .testTag("tab_switch_income"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Income",
                        tint = if (isIncome) MontraIncomeGreen else MontraTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Income",
                        fontSize = 14.sp,
                        fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                        color = if (isIncome) MontraIncomeGreen else MontraTextSecondary
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3B1E22))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = error, color = Color(0xFFF87171), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Smart Receipt Scanning Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MontraSurface)
                    .border(1.dp, MontraButtonBg.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MontraButtonBg.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DocumentScanner,
                                    contentDescription = "OCR",
                                    tint = MontraButtonBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Smart Receipt Scan",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraTextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "Auto-extract date, amount, & merchant",
                                    fontSize = 12.sp,
                                    color = MontraTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isScanningReceipt) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MontraSurfaceElevated)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MontraButtonBg,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Analyzing paper receipt with OCR...",
                                fontSize = 13.sp,
                                color = MontraTextPrimary
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MontraButtonBg,
                                    contentColor = MontraTextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_scan_receipt_picker")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DocumentScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Scan Receipt", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Quick sample receipt OCR button
                            OutlinedButton(
                                onClick = {
                                    // Simulate OCR extraction from sample physical receipt
                                    val sampleMerchants = listOf(
                                        Triple("Starbucks Coffee", 6.85, ExpenseCategory.FOOD),
                                        Triple("Whole Foods Market", 54.20, ExpenseCategory.SHOPPING),
                                        Triple("Shell Gasoline", 38.50, ExpenseCategory.TRANSPORT),
                                        Triple("CVS Pharmacy", 19.95, ExpenseCategory.HEALTH)
                                    )
                                    val picked = sampleMerchants.random()
                                    amountText = String.format(java.util.Locale.US, "%.2f", picked.second)
                                    description = picked.first
                                    selectedCategoryKey = picked.third.name
                                    dateMillis = System.currentTimeMillis()
                                    scanNotice = "OCR Extracted: ${picked.first} • ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", picked.second)}"
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MontraTextSecondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_sample_receipt")
                            ) {
                                Text(text = "Sample Receipt", fontSize = 13.sp)
                            }
                        }
                    }

                    scanNotice?.let { notice ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Scanned",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = notice,
                                fontSize = 12.sp,
                                color = Color(0xFFA7F3D0),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Field
            Text(
                text = "Amount",
                fontSize = 13.sp,
                color = MontraTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MontraSurface)
                    .border(1.dp, MontraBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${selectedCurrency.symbol} ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MontraTextSecondary
                    )
                    BasicTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            errorMessage = null
                        },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MontraTextPrimary
                        ),
                        cursorBrush = SolidColor(MontraTextPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_expense_amount"),
                        decorationBox = { innerTextField ->
                            if (amountText.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MontraTextMuted
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Field
            Text(
                text = "Category",
                fontSize = 13.sp,
                color = MontraTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MontraSurface)
                    .border(1.dp, MontraBorder, RoundedCornerShape(16.dp))
                    .clickable(enabled = !isIncome) { isCategoryPickerOpen = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .testTag("btn_select_category")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(selectedCategoryItem.pastelBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedCategoryItem.icon,
                                contentDescription = null,
                                tint = selectedCategoryItem.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = selectedCategoryItem.displayName,
                                fontSize = 15.sp,
                                color = MontraTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            if (isIncome || selectedCategoryItem.isIncome || selectedCategoryKey.equals(ExpenseCategory.INCOME.name, ignoreCase = true)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MontraIncomeGreen.copy(alpha = 0.18f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Income Category",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraIncomeGreen
                                    )
                                }
                            }
                        }
                    }

                    if (!isIncome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Select",
                            tint = MontraTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Field
            Text(
                text = "Date",
                fontSize = 13.sp,
                color = MontraTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MontraSurface)
                    .border(1.dp, MontraBorder, RoundedCornerShape(16.dp))
                    .clickable { isDatePickerOpen = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .testTag("btn_select_date")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = MontraTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = DateUtils.formatDisplayDate(dateMillis),
                            fontSize = 15.sp,
                            color = MontraTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Select",
                        tint = MontraTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description (optional) Field
            Text(
                text = "Description (optional)",
                fontSize = 13.sp,
                color = MontraTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MontraSurface)
                    .border(1.dp, MontraBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = MontraTextPrimary
                    ),
                    cursorBrush = SolidColor(MontraTextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_description"),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) {
                            Text(
                                text = "e.g. Lunch with friends",
                                fontSize = 15.sp,
                                color = MontraTextMuted
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Bottom Button: Add Expense / Add Income
        val isCurrentlyIncome = isIncome ||
                selectedCategoryKey.equals(ExpenseCategory.INCOME.name, ignoreCase = true) ||
                selectedCategoryKey.equals("SALARY", ignoreCase = true)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }
                    onAddExpense(
                        amount,
                        selectedCategoryKey,
                        dateMillis,
                        description.trim(),
                        isCurrentlyIncome
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentlyIncome) MontraIncomeGreen else MontraButtonBg,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_submit_add_expense")
            ) {
                Text(
                    text = if (isCurrentlyIncome) "Add Income" else "Add Expense",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Category Selector Dialog
    if (isCategoryPickerOpen) {
        Dialog(onDismissRequest = { isCategoryPickerOpen = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(24.dp)),
                color = MontraSurface,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Select Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val expenseCategories = CategoryRegistry.getExpenseCategoryItems()

                    LazyColumn(
                        modifier = Modifier.height(360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(expenseCategories, key = { it.name }) { cat ->
                            val isSelected = selectedCategoryKey.equals(cat.name, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MontraSurfaceElevated else Color.Transparent)
                                    .clickable {
                                        selectedCategoryKey = cat.name
                                        isIncome = false
                                        isCategoryPickerOpen = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("category_select_item_${cat.name}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = MontraTextPrimary
                                    )
                                    if (cat.isCustom) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(cat.color.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Custom",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = cat.color
                                            )
                                        }
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = cat.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MontraBorder)
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (isDatePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dateMillis = it
                        }
                        isDatePickerOpen = false
                    }
                ) {
                    Text("OK", color = MontraTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("Cancel", color = MontraTextMuted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
