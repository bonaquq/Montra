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
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import java.io.File
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.ui.BudgetStatus
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraIncomeGreen
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import com.example.util.AmountInputUtils
import com.example.util.DateUtils
import com.example.util.FormatUtils
import com.example.util.ParsedReceiptData
import com.example.util.ReceiptParser

enum class AddExpenseTab {
    EXPENSE,
    INCOME,
    BUDGET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    onAddExpense: (amount: Double, category: String, dateMillis: Long, description: String, isIncome: Boolean, currencyCode: String) -> Unit,
    onSaveBudget: ((category: String, limit: Double) -> Unit)? = null,
    budgetStatuses: List<BudgetStatus> = emptyList(),
    onScanReceipt: ((Uri) -> Unit)? = null,
    isScanningReceipt: Boolean = false,
    scannedReceiptResult: ParsedReceiptData? = null,
    onClearScannedReceipt: (() -> Unit)? = null,
    selectedCurrency: SupportedCurrency = SupportedCurrency.MVR,
    isDeveloperMode: Boolean = false,
    initialTab: AddExpenseTab = AddExpenseTab.EXPENSE,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(initialTab) }
    var transactionCurrency by remember(selectedCurrency) { mutableStateOf(selectedCurrency) }
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

    // Budget Tab specific state
    var isOverallBudget by remember { mutableStateOf(false) }
    var isDailyBudget by remember { mutableStateOf(false) }
    val daysInMonth = remember { java.util.Calendar.getInstance().getActualMaximum(java.util.Calendar.DAY_OF_MONTH) }
    var budgetCategoryKey by remember { mutableStateOf(ExpenseCategory.FOOD.name) }
    var budgetAmountText by remember { mutableStateOf("") }
    var budgetSuccessMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    var isScanSourceModalOpen by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null && onScanReceipt != null) {
                onScanReceipt(uri)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                cameraTempUri?.let { uri ->
                    if (onScanReceipt != null) {
                        onScanReceipt(uri)
                    }
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                try {
                    val tempFile = File(context.cacheDir, "camera_doc_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        tempFile
                    )
                    cameraTempUri = uri
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    android.util.Log.e("AddExpenseScreen", "Failed to launch camera on permission grant", e)
                }
            }
        }
    )

    fun launchCameraCapture() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                val tempFile = File(context.cacheDir, "camera_doc_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                cameraTempUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                android.util.Log.e("AddExpenseScreen", "Failed to launch camera", e)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(scannedReceiptResult) {
        scannedReceiptResult?.let { data ->
            if (data.amount > 0.0) {
                amountText = String.format(java.util.Locale.US, "%.2f", data.amount)
            }
            if (data.merchantOrTitle.isNotBlank() && data.merchantOrTitle != "Scanned Document") {
                description = data.merchantOrTitle
            }
            if (data.dateMillis > 0L) {
                dateMillis = data.dateMillis
            }
            if (data.isCreditOrIncome) {
                isIncome = true
                currentTab = AddExpenseTab.INCOME
                if (data.currencyCode.isNotBlank()) {
                    transactionCurrency = SupportedCurrency.fromCode(data.currencyCode)
                }
                selectedCategoryKey = if (data.categoryHint.equals("TRANSFER", ignoreCase = true)) {
                    "TRANSFER"
                } else {
                    ExpenseCategory.INCOME.name
                }
            } else {
                isIncome = false
                currentTab = AddExpenseTab.EXPENSE
                if (data.currencyCode.isNotBlank()) {
                    transactionCurrency = SupportedCurrency.fromCode(data.currencyCode)
                }
                val resolvedItem = CategoryRegistry.getCategoryItem(data.categoryHint)
                selectedCategoryKey = resolvedItem.name
            }
            scanNotice = if (data.amount > 0.0) {
                "Auto-extracted: ${data.merchantOrTitle} • ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", data.amount)}"
            } else {
                "Scanned: ${data.merchantOrTitle}"
            }
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
                text = when (currentTab) {
                    AddExpenseTab.EXPENSE -> "Add Expense"
                    AddExpenseTab.INCOME -> "Add Income"
                    AddExpenseTab.BUDGET -> "Add Budget"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MontraTextPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        // Segmented Control Tabs for Expense vs Income vs Budget
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
                    .background(if (currentTab == AddExpenseTab.EXPENSE) MontraSurfaceElevated else Color.Transparent)
                    .clickable {
                        currentTab = AddExpenseTab.EXPENSE
                        isIncome = false
                        if (selectedCategoryKey.equals(ExpenseCategory.INCOME.name, ignoreCase = true) ||
                            selectedCategoryKey.equals("SALARY", ignoreCase = true)
                        ) {
                            selectedCategoryKey = ExpenseCategory.FOOD.name
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
                        tint = if (currentTab == AddExpenseTab.EXPENSE) Color(0xFFF87171) else MontraTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Expense",
                        fontSize = 13.sp,
                        fontWeight = if (currentTab == AddExpenseTab.EXPENSE) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentTab == AddExpenseTab.EXPENSE) MontraTextPrimary else MontraTextSecondary
                    )
                }
            }

            // Income Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (currentTab == AddExpenseTab.INCOME) MontraIncomeGreen.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable {
                        currentTab = AddExpenseTab.INCOME
                        isIncome = true
                        selectedCategoryKey = ExpenseCategory.INCOME.name
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
                        tint = if (currentTab == AddExpenseTab.INCOME) MontraIncomeGreen else MontraTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Income",
                        fontSize = 13.sp,
                        fontWeight = if (currentTab == AddExpenseTab.INCOME) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentTab == AddExpenseTab.INCOME) MontraIncomeGreen else MontraTextSecondary
                    )
                }
            }

            // Budget Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (currentTab == AddExpenseTab.BUDGET) Color(0xFF6366F1).copy(alpha = 0.25f) else Color.Transparent)
                    .clickable {
                        currentTab = AddExpenseTab.BUDGET
                        val targetKey = if (isOverallBudget) "OVERALL" else budgetCategoryKey
                        val existing = budgetStatuses.firstOrNull { it.categoryName == targetKey }
                        if (existing != null && existing.monthlyLimit > 0 && budgetAmountText.isEmpty()) {
                            budgetAmountText = String.format(java.util.Locale.US, "%.0f", existing.monthlyLimit)
                        }
                    }
                    .padding(vertical = 10.dp)
                    .testTag("tab_switch_budget"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = "Budget",
                        tint = if (currentTab == AddExpenseTab.BUDGET) Color(0xFF818CF8) else MontraTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Budget",
                        fontSize = 13.sp,
                        fontWeight = if (currentTab == AddExpenseTab.BUDGET) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentTab == AddExpenseTab.BUDGET) Color(0xFF818CF8) else MontraTextSecondary
                    )
                }
            }
        }

        if (currentTab == AddExpenseTab.BUDGET) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Success banner
                budgetSuccessMessage?.let { success ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = success,
                                color = Color(0xFFA7F3D0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

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

                // Scope selector: Overall vs Category
                Text(
                    text = "Budget Target",
                    fontSize = 13.sp,
                    color = MontraTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MontraSurface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Overall
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isOverallBudget) Color(0xFF6366F1).copy(alpha = 0.25f) else Color.Transparent)
                            .clickable {
                                isOverallBudget = true
                                val existing = budgetStatuses.firstOrNull { it.categoryName == "OVERALL" }
                                if (existing != null && existing.monthlyLimit > 0) {
                                    budgetAmountText = if (isDailyBudget) {
                                        String.format(java.util.Locale.US, "%.1f", existing.monthlyLimit / daysInMonth)
                                    } else {
                                        String.format(java.util.Locale.US, "%.0f", existing.monthlyLimit)
                                    }
                                }
                            }
                            .padding(vertical = 10.dp)
                            .testTag("btn_budget_target_overall"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (isOverallBudget) Color(0xFF818CF8) else MontraTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Overall Cap",
                                fontSize = 13.sp,
                                fontWeight = if (isOverallBudget) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOverallBudget) Color(0xFF818CF8) else MontraTextSecondary
                            )
                        }
                    }

                    // Category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isOverallBudget) Color(0xFF6366F1).copy(alpha = 0.25f) else Color.Transparent)
                            .clickable {
                                isOverallBudget = false
                                val existing = budgetStatuses.firstOrNull { it.categoryName == budgetCategoryKey }
                                if (existing != null && existing.monthlyLimit > 0) {
                                    budgetAmountText = String.format(java.util.Locale.US, "%.0f", existing.monthlyLimit)
                                }
                            }
                            .padding(vertical = 10.dp)
                            .testTag("btn_budget_target_category"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PieChart,
                                contentDescription = null,
                                tint = if (!isOverallBudget) Color(0xFF818CF8) else MontraTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Category Budget",
                                fontSize = 13.sp,
                                fontWeight = if (!isOverallBudget) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isOverallBudget) Color(0xFF818CF8) else MontraTextSecondary
                            )
                        }
                    }
                }

                // Daily / Monthly Toggle when Overall Cap is selected
                if (isOverallBudget) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Budget Period",
                        fontSize = 13.sp,
                        color = MontraTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MontraSurface)
                            .border(1.dp, MontraBorder, RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Monthly option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (!isDailyBudget) Color(0xFF6366F1) else Color.Transparent)
                                .clickable {
                                    if (isDailyBudget) {
                                        val curr = budgetAmountText.toDoubleOrNull()
                                        if (curr != null && curr > 0) {
                                            budgetAmountText = String.format(java.util.Locale.US, "%.0f", curr * daysInMonth)
                                        }
                                        isDailyBudget = false
                                    }
                                }
                                .padding(vertical = 8.dp)
                                .testTag("btn_overall_period_monthly"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Monthly Budget",
                                fontSize = 12.sp,
                                fontWeight = if (!isDailyBudget) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isDailyBudget) Color.White else MontraTextSecondary
                            )
                        }

                        // Daily option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isDailyBudget) Color(0xFF6366F1) else Color.Transparent)
                                .clickable {
                                    if (!isDailyBudget) {
                                        val curr = budgetAmountText.toDoubleOrNull()
                                        if (curr != null && curr > 0) {
                                            budgetAmountText = String.format(java.util.Locale.US, "%.1f", curr / daysInMonth)
                                        }
                                        isDailyBudget = true
                                    }
                                }
                                .padding(vertical = 8.dp)
                                .testTag("btn_overall_period_daily"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Daily Budget",
                                fontSize = 12.sp,
                                fontWeight = if (isDailyBudget) FontWeight.Bold else FontWeight.Medium,
                                color = if (isDailyBudget) Color.White else MontraTextSecondary
                            )
                        }
                    }
                }

                // If Category Budget is selected: Category Selector
                if (!isOverallBudget) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Select Category",
                        fontSize = 13.sp,
                        color = MontraTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentBudgetItem = CategoryRegistry.getCategoryItem(budgetCategoryKey)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MontraSurface)
                            .border(1.dp, MontraBorder, RoundedCornerShape(16.dp))
                            .clickable { isCategoryPickerOpen = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .testTag("btn_select_budget_category")
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
                                        .background(currentBudgetItem.pastelBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = currentBudgetItem.icon,
                                        contentDescription = null,
                                        tint = currentBudgetItem.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = currentBudgetItem.displayName,
                                    fontSize = 15.sp,
                                    color = MontraTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Change",
                                tint = MontraTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Limit Field
                Text(
                    text = if (isOverallBudget && isDailyBudget) "Daily Limit Target" else "Monthly Limit Target",
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
                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedCurrency.symbol,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8),
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        BasicTextField(
                            value = budgetAmountText,
                            onValueChange = { budgetAmountText = AmountInputUtils.sanitizeAmount(it) },
                            textStyle = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontraTextPrimary
                            ),
                            cursorBrush = SolidColor(Color(0xFF818CF8)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_budget_limit"),
                            decorationBox = { innerTextField ->
                                if (budgetAmountText.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MontraTextMuted
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                val enteredBudgetVal = budgetAmountText.toDoubleOrNull() ?: 0.0
                if (enteredBudgetVal > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOverallBudget && isDailyBudget) {
                            "Daily Budget: ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", enteredBudgetVal)}/day (≈ ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", enteredBudgetVal * daysInMonth)} total monthly)"
                        } else if (isOverallBudget) {
                            "Monthly Budget: ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", enteredBudgetVal)} (≈ ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", enteredBudgetVal / daysInMonth)}/day daily allowance)"
                        } else {
                            "Category Budget: ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", enteredBudgetVal)}/month (≈ ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", enteredBudgetVal / daysInMonth)}/day)"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF818CF8),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Quick Preset Chips
                Spacer(modifier = Modifier.height(12.dp))
                val quickPresets = if (isOverallBudget && isDailyBudget) {
                    listOf(15.0, 30.0, 50.0, 100.0)
                } else {
                    listOf(100.0, 250.0, 500.0, 1000.0)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPresets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MontraSurface)
                                .border(0.5.dp, MontraBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    budgetAmountText = String.format(java.util.Locale.US, "%.0f", preset)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${selectedCurrency.symbol}${preset.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MontraTextSecondary
                            )
                        }
                    }
                }

                // Live Spending & Current Status Card
                val activeKey = if (isOverallBudget) "OVERALL" else budgetCategoryKey
                val currentStatus = budgetStatuses.firstOrNull { it.categoryName == activeKey }
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MontraSurface)
                        .border(1.dp, MontraBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isOverallBudget) {
                                        if (isDailyBudget) "Overall Daily Status" else "Overall Monthly Status"
                                    } else {
                                        "${CategoryRegistry.getCategoryItem(budgetCategoryKey).displayName} Status"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontraTextPrimary
                                )
                            }
                            if (currentStatus != null && currentStatus.monthlyLimit > 0) {
                                val isExceeded = if (isOverallBudget && isDailyBudget) {
                                    currentStatus.todaySpent > currentStatus.dailyLimit && currentStatus.dailyLimit > 0
                                } else {
                                    currentStatus.percentUsed >= 100f
                                }
                                val isApproaching = if (isOverallBudget && isDailyBudget) {
                                    currentStatus.dailyLimit > 0 && (currentStatus.todaySpent / currentStatus.dailyLimit) >= 0.8f && !isExceeded
                                } else {
                                    currentStatus.percentUsed >= 80f && currentStatus.percentUsed < 100f
                                }
                                val statusColor = when {
                                    isExceeded -> Color(0xFFF87171)
                                    isApproaching -> Color(0xFFFBBF24)
                                    else -> Color(0xFF10B981)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when {
                                            isExceeded -> "Exceeded"
                                            isApproaching -> "Warning (80%+)"
                                            else -> "Healthy"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }

                        if (currentStatus != null && currentStatus.monthlyLimit > 0) {
                            if (isOverallBudget && isDailyBudget) {
                                val dailyExceeded = currentStatus.todaySpent > currentStatus.dailyLimit && currentStatus.dailyLimit > 0
                                val dailyApproaching = currentStatus.dailyLimit > 0 && (currentStatus.todaySpent / currentStatus.dailyLimit) >= 0.8f && !dailyExceeded
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Spent Today", fontSize = 11.sp, color = MontraTextSecondary)
                                        Text(
                                            "${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", currentStatus.todaySpent)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontraTextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Daily Limit", fontSize = 11.sp, color = MontraTextSecondary)
                                        Text(
                                            "${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", currentStatus.dailyLimit)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF818CF8)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Daily Remaining", fontSize = 11.sp, color = MontraTextSecondary)
                                        Text(
                                            "${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", currentStatus.dailyRemaining)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentStatus.dailyRemaining < 0) Color(0xFFF87171) else Color(0xFF10B981)
                                        )
                                    }
                                }

                                val dailyProgress = if (currentStatus.dailyLimit > 0) {
                                    (currentStatus.todaySpent / currentStatus.dailyLimit).toFloat().coerceIn(0f, 1f)
                                } else 0f

                                LinearProgressIndicator(
                                    progress = { dailyProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = when {
                                        dailyExceeded -> Color(0xFFF87171)
                                        dailyApproaching -> Color(0xFFFBBF24)
                                        else -> Color(0xFF10B981)
                                    },
                                    trackColor = MontraBorder
                                )
                            } else {
                                val isExceeded = currentStatus.percentUsed >= 100f
                                val isApproaching = currentStatus.percentUsed >= 80f && currentStatus.percentUsed < 100f
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Spent this Month", fontSize = 11.sp, color = MontraTextSecondary)
                                        Text(
                                            "${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", currentStatus.currentSpent)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontraTextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Current Limit", fontSize = 11.sp, color = MontraTextSecondary)
                                        Text(
                                            "${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", currentStatus.monthlyLimit)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF818CF8)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Remaining", fontSize = 11.sp, color = MontraTextSecondary)
                                        Text(
                                            "${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", currentStatus.remaining)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentStatus.remaining < 0) Color(0xFFF87171) else Color(0xFF10B981)
                                        )
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { (currentStatus.percentUsed / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = when {
                                        isExceeded -> Color(0xFFF87171)
                                        isApproaching -> Color(0xFFFBBF24)
                                        else -> Color(0xFF10B981)
                                    },
                                    trackColor = MontraBorder
                                )
                            }
                        } else {
                            Text(
                                text = "No budget configured yet for this target. Setting a limit will activate tracking, progress bars, and smart warnings before you overspend.",
                                fontSize = 12.sp,
                                color = MontraTextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MontraSurfaceElevated)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Savings,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Budgets automatically track monthly spending and trigger alerts when you approach or exceed limits.",
                        fontSize = 12.sp,
                        color = MontraTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
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

            // Smart Receipt Scanning Card (Developer Mode Exclusive)
            if (isDeveloperMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MontraSurface)
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
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
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DocumentScanner,
                                        contentDescription = "OCR",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Smart Scanner",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontraTextPrimary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF8B5CF6))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                .testTag("badge_smart_scanner_dev")
                                        ) {
                                            Text(
                                                text = "DEV",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF059669))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "OCR",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Scan BML/MIB bank statements, STELCO/MWSC bills & receipts",
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
                                    color = Color(0xFF10B981),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Analyzing statement/bill with OCR...",
                                    fontSize = 13.sp,
                                    color = MontraTextPrimary
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    isScanSourceModalOpen = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF059669),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("btn_scan_receipt_picker")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DocumentScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Scan Document / Photo (Statement, Bill)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
            }

            // Scan Document Source Selection Dialog (Camera or Photos)
            if (isScanSourceModalOpen) {
                Dialog(onDismissRequest = { isScanSourceModalOpen = false }) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MontraSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DocumentScanner,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Scan Document / Statement",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraTextPrimary
                                    )
                                }
                                IconButton(onClick = { isScanSourceModalOpen = false }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = MontraTextMuted)
                                }
                            }

                            Text(
                                text = "Choose how you would like to scan your bank statement or utility bill:",
                                fontSize = 13.sp,
                                color = MontraTextSecondary,
                                lineHeight = 18.sp
                            )

                            // Option 1: Take Photo with Camera
                            Surface(
                                onClick = {
                                    isScanSourceModalOpen = false
                                    launchCameraCapture()
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF064E3B).copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PhotoCamera,
                                            contentDescription = "Camera",
                                            tint = Color.Black,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Scan with Camera",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MontraTextPrimary
                                        )
                                        Text(
                                            text = "Take a photo of paper bill or statement directly",
                                            fontSize = 12.sp,
                                            color = MontraTextMuted
                                        )
                                    }
                                }
                            }

                            // Option 2: Choose from Photos
                            Surface(
                                onClick = {
                                    isScanSourceModalOpen = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = MontraSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MontraBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PhotoLibrary,
                                            contentDescription = "Photos",
                                            tint = MontraTextPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Choose from Photos",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MontraTextPrimary
                                        )
                                        Text(
                                            text = "Select screenshot or image from gallery",
                                            fontSize = 12.sp,
                                            color = MontraTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val isCurrentlyIncomeSection = currentTab == AddExpenseTab.INCOME || isIncome ||
                    selectedCategoryKey.equals(ExpenseCategory.INCOME.name, ignoreCase = true) ||
                    selectedCategoryKey.equals("SALARY", ignoreCase = true)

            // Currency Selector (Multi-currency switcher: MVR, USD, EUR, GBP, JPY, INR, CAD, AUD)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCurrentlyIncomeSection) "Income Currency" else "Expense Currency",
                    fontSize = 13.sp,
                    color = MontraTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${transactionCurrency.displayName} (${transactionCurrency.symbol})",
                    fontSize = 12.sp,
                    color = if (isCurrentlyIncomeSection) Color(0xFF34D399) else Color(0xFF818CF8),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MontraSurface)
                    .border(1.dp, MontraBorder, RoundedCornerShape(14.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val allCurrencies = listOf(
                    Triple(SupportedCurrency.MVR, "MVR (Rf)", "🇲🇻"),
                    Triple(SupportedCurrency.USD, "USD ($)", "🇺🇸"),
                    Triple(SupportedCurrency.EUR, "EUR (€)", "🇪🇺"),
                    Triple(SupportedCurrency.GBP, "GBP (£)", "🇬🇧"),
                    Triple(SupportedCurrency.JPY, "JPY (¥)", "🇯🇵"),
                    Triple(SupportedCurrency.INR, "INR (₹)", "🇮🇳"),
                    Triple(SupportedCurrency.CAD, "CAD (CA$)", "🇨🇦"),
                    Triple(SupportedCurrency.AUD, "AUD (A$)", "🇦🇺")
                )
                allCurrencies.forEach { (curr, label, flag) ->
                    val isSelected = transactionCurrency == curr
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { transactionCurrency = curr }
                            .testTag("btn_currency_${curr.code.lowercase()}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) {
                            if (isCurrentlyIncomeSection) Color(0xFF059669) else Color(0xFF4F46E5)
                        } else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) {
                                if (isCurrentlyIncomeSection) Color(0xFF34D399) else Color(0xFF818CF8)
                            } else MontraBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = flag, fontSize = 14.sp)
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MontraTextSecondary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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
                        text = "${transactionCurrency.symbol} ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MontraTextSecondary
                    )
                    BasicTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = AmountInputUtils.sanitizeAmount(it)
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

            // Live foreign currency conversion & spending notice
            if (transactionCurrency != selectedCurrency) {
                val parsedAmt = amountText.toDoubleOrNull() ?: 0.0
                val convertedVal = SupportedCurrency.convert(if (parsedAmt > 0) parsedAmt else 1.0, transactionCurrency, selectedCurrency)
                val rate = SupportedCurrency.convert(1.0, transactionCurrency, selectedCurrency)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF312E81).copy(alpha = 0.35f))
                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (parsedAmt > 0) {
                                "≈ ${FormatUtils.formatCurrency(convertedVal, selectedCurrency)} (${selectedCurrency.code})"
                            } else {
                                "Exchange Rate: 1 ${transactionCurrency.code} = ${String.format(java.util.Locale.US, "%.2f", rate)} ${selectedCurrency.code}"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE0E7FF)
                        )
                    }
                    Text(
                        text = if (isCurrentlyIncomeSection) {
                            "Added to separate ${transactionCurrency.code} balance • Does not inflate ${selectedCurrency.code}"
                        } else {
                            "Deducted from separate ${transactionCurrency.code} balance • Does not deduct from ${selectedCurrency.code}"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFFC7D2FE)
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

            // Category Budget Live Glance (on Expense tab)
            if (currentTab == AddExpenseTab.EXPENSE) {
                val catBudget = budgetStatuses.firstOrNull { it.categoryName == selectedCategoryKey }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MontraSurface)
                        .border(1.dp, MontraBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (catBudget != null && catBudget.monthlyLimit > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Budget: ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.0f", catBudget.currentSpent)} / ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.0f", catBudget.monthlyLimit)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MontraTextPrimary
                                    )
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.0f", catBudget.percentUsed)}% spent • ${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.0f", catBudget.remaining)} left",
                                        fontSize = 11.sp,
                                        color = if (catBudget.percentUsed >= 100f) Color(0xFFF87171) else MontraTextSecondary
                                    )
                                }
                            }
                            TextButton(
                                onClick = {
                                    currentTab = AddExpenseTab.BUDGET
                                    isOverallBudget = false
                                    budgetCategoryKey = selectedCategoryKey
                                    budgetAmountText = String.format(java.util.Locale.US, "%.0f", catBudget.monthlyLimit)
                                },
                                modifier = Modifier.testTag("btn_adjust_category_budget")
                            ) {
                                Text(
                                    text = "Adjust",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MontraSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Savings,
                                        contentDescription = null,
                                        tint = MontraTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "No budget set for ${selectedCategoryItem.displayName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MontraTextPrimary
                                    )
                                    Text(
                                        text = "Set a monthly spending limit to prevent overspending",
                                        fontSize = 10.sp,
                                        color = MontraTextMuted
                                    )
                                }
                            }
                            TextButton(
                                onClick = {
                                    currentTab = AddExpenseTab.BUDGET
                                    isOverallBudget = false
                                    budgetCategoryKey = selectedCategoryKey
                                    budgetAmountText = ""
                                },
                                modifier = Modifier.testTag("btn_add_category_budget")
                            ) {
                                Text(
                                    text = "+ Add Budget",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8)
                                )
                            }
                        }
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
    }

        // Bottom Button: Add Expense / Add Income / Save Budget
        if (currentTab == AddExpenseTab.BUDGET) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                val activeKey = if (isOverallBudget) "OVERALL" else budgetCategoryKey
                val currentStatus = budgetStatuses.firstOrNull { it.categoryName == activeKey }
                val isUpdate = currentStatus != null && currentStatus.monthlyLimit > 0
                Button(
                    onClick = {
                        val inputVal = budgetAmountText.toDoubleOrNull()
                        if (inputVal == null || inputVal <= 0.0) {
                            errorMessage = "Please enter a valid budget limit amount"
                            return@Button
                        }
                        val targetKey = if (isOverallBudget) "OVERALL" else budgetCategoryKey
                        val finalMonthlyLimit = if (isOverallBudget && isDailyBudget) inputVal * daysInMonth else inputVal
                        val targetDisplayName = if (isOverallBudget) {
                            if (isDailyBudget) "Overall Daily Budget (${selectedCurrency.symbol}${String.format(java.util.Locale.US, "%.2f", inputVal)}/day)" else "Overall Monthly Budget"
                        } else {
                            CategoryRegistry.getCategoryItem(budgetCategoryKey).displayName
                        }
                        onSaveBudget?.invoke(targetKey, finalMonthlyLimit)
                        budgetSuccessMessage = "Saved $targetDisplayName successfully!"
                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_save_monthly_budget")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isUpdate) "Update Budget" else "Add Budget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
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
                            isCurrentlyIncome,
                            transactionCurrency.code
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlyIncome) Color(0xFF059669) else MontraButtonBg,
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
                            val currentKey = if (currentTab == AddExpenseTab.BUDGET) budgetCategoryKey else selectedCategoryKey
                            val isSelected = currentKey.equals(cat.name, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MontraSurfaceElevated else Color.Transparent)
                                    .clickable {
                                        if (currentTab == AddExpenseTab.BUDGET) {
                                            budgetCategoryKey = cat.name
                                            val existing = budgetStatuses.firstOrNull { it.categoryName == cat.name }
                                            if (existing != null && existing.monthlyLimit > 0) {
                                                budgetAmountText = String.format(java.util.Locale.US, "%.0f", existing.monthlyLimit)
                                            }
                                        } else {
                                            selectedCategoryKey = cat.name
                                            isIncome = false
                                        }
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
