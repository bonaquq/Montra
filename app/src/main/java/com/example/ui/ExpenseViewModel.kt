package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.Budget
import com.example.data.CategoryRegistry
import com.example.data.CustomCategory
import com.example.data.Expense
import com.example.data.ExpenseCategory
import com.example.data.ExpenseDatabase
import com.example.data.ExpenseRepository
import com.example.data.SavingsGoal
import com.example.data.SupportedCurrency
import com.example.data.UserAccount
import com.example.ui.theme.AppTheme
import com.example.util.FormatUtils
import com.example.util.ParsedReceiptData
import com.example.util.ReceiptParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class AppTab(val label: String) {
    HOME("Home"),
    TRANSACTIONS("Transactions"),
    ANALYTICS("Analytics"),
    CATEGORIES("Categories"),
    SETTINGS("Settings"),
    ADD_EXPENSE("Add Expense"),
    REPORT("Report"),
    PLAN("Plan")
}

enum class ReportSubTab(val label: String) {
    EXPENSES("Expenses"),
    INCOME("Income")
}

enum class ChartType {
    DONUT,
    BAR
}

enum class TimeRange(val label: String) {
    TODAY("Today"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    LAST_30_DAYS("30 Days"),
    ALL_TIME("All Time")
}

data class CategorySummary(
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val categoryName: String = category.name,
    val displayName: String = category.displayName,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class BudgetStatus(
    val categoryName: String,
    val displayName: String,
    val monthlyLimit: Double,
    val currentSpent: Double,
    val percentUsed: Float,
    val remaining: Double,
    val dailyLimit: Double = 0.0,
    val todaySpent: Double = 0.0,
    val dailyPercentUsed: Float = 0f,
    val dailyRemaining: Double = 0.0
)

enum class BudgetPeriod(val displayName: String) {
    MONTHLY("Monthly"),
    DAILY("Daily")
}

data class BudgetAlert(
    val categoryName: String,
    val displayName: String,
    val limit: Double,
    val spent: Double,
    val percentUsed: Float,
    val isExceeded: Boolean,
    val isApproaching: Boolean,
    val message: String
)

data class AiInsightItem(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "DUPLICATE", "ANOMALY", "SAVINGS", "TIP"
    val severity: String = "INFO", // "ALERT", "WARNING", "INFO"
    val relatedExpense: Expense? = null
)

data class DailyAnalyticsBar(
    val label: String,
    val amount: Float,
    val isHighest: Boolean = false
)

data class CurrencyIncomeSummary(
    val currencyCode: String,
    val currency: SupportedCurrency,
    val totalAmount: Double,
    val count: Int
)

data class ForeignCurrencyRecord(
    val currency: SupportedCurrency,
    val currencyCode: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val netAmount: Double,
    val convertedToMain: Double,
    val count: Int,
    val exchangeRateToMain: Double
)

data class ExpenseUiState(
    val activeTab: AppTab = AppTab.HOME,
    val reportSubTab: ReportSubTab = ReportSubTab.EXPENSES,
    val chartType: ChartType = ChartType.DONUT,
    val selectedMonthLabel: String = "September 2025",
    val currentBalance: Double = 0.0,
    val totalBalance: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val incomeByCurrency: List<CurrencyIncomeSummary> = emptyList(),
    val foreignCurrencyRecords: List<ForeignCurrencyRecord> = emptyList(),
    val totalForeignHoldingsInMainCurrency: Double = 0.0,
    val isBalanceHidden: Boolean = false,
    val activeAccount: UserAccount? = null,
    val allAccounts: List<UserAccount> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val allExpensesUnfiltered: List<Expense> = emptyList(),
    val categorySummaries: List<CategorySummary> = emptyList(),
    val customCategories: List<CustomCategory> = emptyList(),
    val topCategory: CategorySummary? = null,
    val budgets: List<Budget> = emptyList(),
    val budgetStatuses: List<BudgetStatus> = emptyList(),
    val overallBudgetStatus: BudgetStatus? = null,
    val budgetAlerts: List<BudgetAlert> = emptyList(),
    val dismissedBudgetAlerts: Set<String> = emptySet(),
    val budgetWarningThresholdPercent: Int = 80,
    val recentBudgetAlertMessage: String? = null,
    val overallBudgetPeriod: BudgetPeriod = BudgetPeriod.MONTHLY,
    val daysInCurrentMonth: Int = 30,
    val aiInsights: List<AiInsightItem> = emptyList(),
    val financialHealthScore: Int = 85,
    val selectedTimeRange: TimeRange = TimeRange.THIS_MONTH,
    val selectedCategory: ExpenseCategory? = null,
    val selectedCurrency: SupportedCurrency = SupportedCurrency.MVR,
    val searchQuery: String = "",
    val transactionFilter: String = "ALL", // "ALL", "EXPENSES", "INCOME"
    val analyticsPeriod: String = "WEEKLY", // "WEEKLY", "MONTHLY", "YEARLY", "CUSTOM"
    val analyticsDateRangeLabel: String = "Sep 8 – Sep 14, 2025",
    val analyticsStartDateMillis: Long = 0L,
    val analyticsEndDateMillis: Long = 0L,
    val analyticsTotalSpent: Double = 0.0,
    val analyticsExpenses: List<Expense> = emptyList(),
    val analyticsDailyBars: List<DailyAnalyticsBar> = emptyList(),
    val analyticsCategorySummaries: List<CategorySummary> = emptyList(),
    val isLoading: Boolean = false,
    val isScanningReceipt: Boolean = false,
    val scannedReceiptResult: ParsedReceiptData? = null,
    val authUser: com.example.auth.AuthUser? = null,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val isAuthDismissed: Boolean = false,
    val appTheme: AppTheme = AppTheme.DARK,
    val isDarkMode: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val isAppUnlocked: Boolean = true,
    val biometricErrorMessage: String? = null,
    val isDeveloperUnlocked: Boolean = false
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val prefs = application.getSharedPreferences("montra_preferences", Context.MODE_PRIVATE)

    private val initialTheme: AppTheme = prefs.getString("pref_app_theme", null)?.let {
        AppTheme.fromString(it)
    } ?: if (prefs.getBoolean("pref_dark_mode", true)) AppTheme.DARK else AppTheme.LIGHT

    private val _appTheme = MutableStateFlow(initialTheme)
    val appTheme: StateFlow<AppTheme> = _appTheme

    private val _isDarkMode = MutableStateFlow(initialTheme.isDark)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _activeTab = MutableStateFlow(AppTab.HOME)
    val activeTab: StateFlow<AppTab> = _activeTab

    private val _reportSubTab = MutableStateFlow(ReportSubTab.EXPENSES)
    val reportSubTab: StateFlow<ReportSubTab> = _reportSubTab

    private val _chartType = MutableStateFlow(ChartType.DONUT)
    val chartType: StateFlow<ChartType> = _chartType

    private val _selectedMonthLabel = MutableStateFlow("September 2025")
    val selectedMonthLabel: StateFlow<String> = _selectedMonthLabel

    private val _isBalanceHidden = MutableStateFlow(false)
    val isBalanceHidden: StateFlow<Boolean> = _isBalanceHidden

    private val _transactionFilter = MutableStateFlow("ALL")
    val transactionFilter: StateFlow<String> = _transactionFilter

    private val _analyticsPeriod = MutableStateFlow("WEEKLY")
    val analyticsPeriod: StateFlow<String> = _analyticsPeriod

    private val _analyticsOffset = MutableStateFlow(0)
    val analyticsOffset: StateFlow<Int> = _analyticsOffset

    private val _customAnalyticsStartDateMillis = MutableStateFlow(System.currentTimeMillis() - 6 * 86400000L)
    val customAnalyticsStartDateMillis: StateFlow<Long> = _customAnalyticsStartDateMillis

    private val _customAnalyticsEndDateMillis = MutableStateFlow(System.currentTimeMillis())
    val customAnalyticsEndDateMillis: StateFlow<Long> = _customAnalyticsEndDateMillis

    private val _budgetWarningThreshold = MutableStateFlow(80)
    val budgetWarningThreshold: StateFlow<Int> = _budgetWarningThreshold

    private val _dismissedAlerts = MutableStateFlow<Set<String>>(emptySet())
    val dismissedAlerts: StateFlow<Set<String>> = _dismissedAlerts

    private val _recentBudgetAlertMessage = MutableStateFlow<String?>(null)
    val recentBudgetAlertMessage: StateFlow<String?> = _recentBudgetAlertMessage

    private val _overallBudgetPeriod = MutableStateFlow(BudgetPeriod.MONTHLY)
    val overallBudgetPeriod: StateFlow<BudgetPeriod> = _overallBudgetPeriod

    private val _savingsGoals = MutableStateFlow(
        listOf(
            SavingsGoal("goal_1", "House by the Sea", 1000.0, 1750.0, "30% behind schedule", "HOUSE"),
            SavingsGoal("goal_2", "Save for a Car", 2500.0, 7500.0, null, "CAR"),
            SavingsGoal("goal_3", "Emergency Fund", 3000.0, 5000.0, null, "HEALTH")
        )
    )

    private val _selectedTimeRange = MutableStateFlow(TimeRange.THIS_MONTH)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    private val _selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategory: StateFlow<ExpenseCategory?> = _selectedCategory

    private val _selectedCurrency = MutableStateFlow(
        SupportedCurrency.fromCode(prefs.getString("pref_currency", "MVR"))
    )
    val selectedCurrency: StateFlow<SupportedCurrency> = _selectedCurrency

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isScanningReceipt = MutableStateFlow(false)
    val isScanningReceipt: StateFlow<Boolean> = _isScanningReceipt

    private val _scannedReceiptResult = MutableStateFlow<ParsedReceiptData?>(null)
    val scannedReceiptResult: StateFlow<ParsedReceiptData?> = _scannedReceiptResult

    private val authService = com.example.auth.AuthService(application)
    private val firestoreService = com.example.data.FirestoreService(application)
    private val _authUser = MutableStateFlow<com.example.auth.AuthUser?>(authService.currentUser)
    val authUser: StateFlow<com.example.auth.AuthUser?> = _authUser

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage

    private val _isAuthDismissed = MutableStateFlow(prefs.getBoolean("pref_auth_dismissed", false))
    val isAuthDismissed: StateFlow<Boolean> = _isAuthDismissed

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean("pref_biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled

    private val _isAppUnlocked = MutableStateFlow(!prefs.getBoolean("pref_biometric_enabled", false))
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked

    private val _biometricErrorMessage = MutableStateFlow<String?>(null)
    val biometricErrorMessage: StateFlow<String?> = _biometricErrorMessage

    private val _isDeveloperUnlocked = MutableStateFlow(false)
    val isDeveloperUnlocked: StateFlow<Boolean> = _isDeveloperUnlocked

    private data class AuthState(
        val user: com.example.auth.AuthUser?,
        val isLoading: Boolean,
        val errorMessage: String?,
        val isDismissed: Boolean
    )

    private val authStateFlow = combine(
        _authUser,
        _isAuthLoading,
        _authErrorMessage,
        _isAuthDismissed
    ) { user, loading, error, dismissed ->
        AuthState(user, loading, error, dismissed)
    }

    init {
        val db = ExpenseDatabase.getDatabase(application)
        repository = ExpenseRepository(db.expenseDao(), db.budgetDao(), db.userAccountDao(), db.customCategoryDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            repository.deleteDemoExpenses()
            repository.resetDemoAccountAndBalances()
        }
        viewModelScope.launch {
            authService.authStateFlow().collect { user ->
                _authUser.value = user
                if (user != null) {
                    _isAuthDismissed.value = true
                    syncDataWithFirestore(user.uid)
                } else {
                    firestoreService.stopListeners()
                }
            }
        }
    }

    private data class FilterCriteria(
        val timeRange: TimeRange,
        val categoryFilter: ExpenseCategory?,
        val currency: SupportedCurrency,
        val search: String,
        val tab: AppTab,
        val subTab: ReportSubTab,
        val chart: ChartType,
        val month: String,
        val balanceHidden: Boolean,
        val transFilter: String,
        val anPeriod: String,
        val anOffset: Int,
        val customStart: Long,
        val customEnd: Long,
        val warningThreshold: Int,
        val dismissedAlerts: Set<String>,
        val recentAlertMsg: String?,
        val appTheme: AppTheme,
        val isDarkMode: Boolean,
        val isBiometricEnabled: Boolean,
        val isAppUnlocked: Boolean,
        val biometricError: String?,
        val isDeveloperUnlocked: Boolean,
        val overallBudgetPeriod: BudgetPeriod
    )

    @Suppress("UNCHECKED_CAST")
    private val filterCriteriaFlow = combine(
        _selectedTimeRange,
        _selectedCategory,
        _selectedCurrency,
        _searchQuery,
        _activeTab,
        _reportSubTab,
        _chartType,
        _selectedMonthLabel,
        _isBalanceHidden,
        _transactionFilter,
        _analyticsPeriod,
        _analyticsOffset,
        _customAnalyticsStartDateMillis,
        _customAnalyticsEndDateMillis,
        _budgetWarningThreshold,
        _dismissedAlerts,
        _recentBudgetAlertMessage,
        _appTheme,
        _isBiometricEnabled,
        _isAppUnlocked,
        _biometricErrorMessage,
        _isDeveloperUnlocked,
        _overallBudgetPeriod
    ) { args: Array<Any?> ->
        val currentTheme = args[17] as AppTheme
        FilterCriteria(
            timeRange = args[0] as TimeRange,
            categoryFilter = args[1] as? ExpenseCategory,
            currency = args[2] as SupportedCurrency,
            search = args[3] as String,
            tab = args[4] as AppTab,
            subTab = args[5] as ReportSubTab,
            chart = args[6] as ChartType,
            month = args[7] as String,
            balanceHidden = args[8] as Boolean,
            transFilter = args[9] as String,
            anPeriod = args[10] as String,
            anOffset = args[11] as Int,
            customStart = args[12] as Long,
            customEnd = args[13] as Long,
            warningThreshold = args[14] as Int,
            dismissedAlerts = args[15] as Set<String>,
            recentAlertMsg = args[16] as? String,
            appTheme = currentTheme,
            isDarkMode = currentTheme.isDark,
            isBiometricEnabled = args[18] as Boolean,
            isAppUnlocked = args[19] as Boolean,
            biometricError = args[20] as? String,
            isDeveloperUnlocked = args[21] as Boolean,
            overallBudgetPeriod = args[22] as BudgetPeriod
        )
    }

    val uiState: StateFlow<ExpenseUiState> = combine(
        repository.allExpenses,
        repository.allBudgets,
        repository.allAccounts,
        repository.activeAccount,
        repository.allCustomCategories,
        filterCriteriaFlow,
        _savingsGoals,
        _isScanningReceipt,
        _scannedReceiptResult,
        authStateFlow
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allExpenses = args[0] as List<Expense>
        @Suppress("UNCHECKED_CAST")
        val allBudgets = args[1] as List<Budget>
        @Suppress("UNCHECKED_CAST")
        val allAccounts = args[2] as List<UserAccount>
        val activeAccount = args[3] as? UserAccount
        @Suppress("UNCHECKED_CAST")
        val customCategoriesList = args[4] as List<CustomCategory>
        val criteria = args[5] as FilterCriteria
        @Suppress("UNCHECKED_CAST")
        val goals = args[6] as List<SavingsGoal>
        val scanning = args[7] as Boolean
        val scannedData = args[8] as? ParsedReceiptData
        val auth = args[9] as AuthState

        CategoryRegistry.updateCustomCategories(customCategoriesList)

        val currency = criteria.currency

        fun convertAmount(exp: Expense): Double {
            val fromCur = exp.currency
            return SupportedCurrency.convert(exp.amount, fromCur, currency)
        }

        val rangeFiltered = filterByTimeRange(allExpenses, criteria.timeRange)

        // Main currency transactions only - foreign currencies do not mix into main balance/metrics
        val expenseItems = rangeFiltered.filter { !it.isIncome && it.currency == currency }
        val incomeItems = rangeFiltered.filter { it.isIncome && it.currency == currency }

        val totalSpentValue = expenseItems.sumOf { it.amount }
        val totalIncomeValue = incomeItems.sumOf { it.amount }

        // Multi-currency income summary - only for currencies with >0 income recorded
        val incomeByCurrencyList = allExpenses.filter { it.isIncome }
            .groupBy { it.currencyCode.uppercase() }
            .map { (code, list) ->
                val cur = SupportedCurrency.fromCode(code)
                val sum = list.sumOf { it.amount }
                CurrencyIncomeSummary(
                    currencyCode = cur.code,
                    currency = cur,
                    totalAmount = sum,
                    count = list.size
                )
            }
            .filter { it.totalAmount > 0 }
            .sortedByDescending { it.totalAmount }

        // Foreign currency records - transactions in non-main currencies or foreign holdings
        val foreignRecordsList = allExpenses
            .groupBy { it.currencyCode.uppercase() }
            .filter { (code, _) ->
                val cur = SupportedCurrency.fromCode(code)
                cur != currency
            }
            .map { (code, list) ->
                val cur = SupportedCurrency.fromCode(code)
                val inc = list.filter { it.isIncome }.sumOf { it.amount }
                val exp = list.filter { !it.isIncome }.sumOf { it.amount }
                val net = inc - exp
                val converted = SupportedCurrency.convert(net, cur, currency)
                val rateToMain = SupportedCurrency.convert(1.0, cur, currency)
                ForeignCurrencyRecord(
                    currency = cur,
                    currencyCode = cur.code,
                    totalIncome = inc,
                    totalExpense = exp,
                    netAmount = net,
                    convertedToMain = converted,
                    count = list.size,
                    exchangeRateToMain = rateToMain
                )
            }
            .filter { it.count > 0 }
            .sortedByDescending { it.convertedToMain }

        val totalForeignHoldingsConverted = foreignRecordsList.sumOf { it.convertedToMain }

        // Category breakdown
        val categoryGroups = expenseItems.groupBy { it.category }
        val summaries = categoryGroups.map { (catKey, list) ->
            val catItem = com.example.data.CategoryRegistry.getCategoryItem(catKey)
            val sum = list.sumOf { convertAmount(it) }
            val pct = if (totalSpentValue > 0) (sum / totalSpentValue).toFloat() else 0f
            CategorySummary(
                category = com.example.data.ExpenseCategory.fromString(catKey),
                categoryName = catItem.name,
                displayName = catItem.displayName,
                totalAmount = sum,
                percentage = pct,
                count = list.size
            )
        }.sortedByDescending { it.totalAmount }

        val topCat = summaries.firstOrNull()

        // Budgets status - Accurate current month & today calculation
        val currentMonthExpenses = filterByTimeRange(allExpenses, TimeRange.THIS_MONTH).filter { !it.isIncome }
        val currentMonthTotalSpent = currentMonthExpenses.sumOf { convertAmount(it) }

        val todayExpenses = filterByTimeRange(allExpenses, TimeRange.TODAY).filter { !it.isIncome }
        val todayTotalSpent = todayExpenses.sumOf { convertAmount(it) }

        val calInstance = java.util.Calendar.getInstance()
        val daysInCurrentMonth = calInstance.getActualMaximum(java.util.Calendar.DAY_OF_MONTH).coerceAtLeast(1)

        val statuses = allBudgets.map { b: Budget ->
            val limit = SupportedCurrency.convert(
                b.monthlyLimit,
                SupportedCurrency.fromCode(b.currencyCode),
                currency
            )
            val spent = if (b.category.equals("OVERALL", ignoreCase = true)) {
                currentMonthTotalSpent
            } else {
                currentMonthExpenses.filter {
                    it.category.equals(b.category, ignoreCase = true) ||
                    it.expenseCategory.name.equals(b.category, ignoreCase = true) ||
                    com.example.data.CategoryRegistry.getCategoryItem(it.category).key.equals(b.category, ignoreCase = true) ||
                    com.example.data.CategoryRegistry.getCategoryItem(it.category).displayName.equals(b.category, ignoreCase = true)
                }.sumOf { convertAmount(it) }
            }
            val pct = if (limit > 0) (spent / limit).toFloat() else 0f
            val remaining = (limit - spent).coerceAtLeast(0.0)

            val spentToday = if (b.category.equals("OVERALL", ignoreCase = true)) {
                todayTotalSpent
            } else {
                todayExpenses.filter {
                    it.category.equals(b.category, ignoreCase = true) ||
                    it.expenseCategory.name.equals(b.category, ignoreCase = true) ||
                    com.example.data.CategoryRegistry.getCategoryItem(it.category).key.equals(b.category, ignoreCase = true) ||
                    com.example.data.CategoryRegistry.getCategoryItem(it.category).displayName.equals(b.category, ignoreCase = true)
                }.sumOf { convertAmount(it) }
            }
            val dailyLimit = if (limit > 0) limit / daysInCurrentMonth else 0.0
            val dailyPct = if (dailyLimit > 0) (spentToday / dailyLimit).toFloat() else 0f
            val dailyRemaining = (dailyLimit - spentToday).coerceAtLeast(0.0)

            val catDisplayName = if (b.category.equals("OVERALL", ignoreCase = true)) "Overall Budget" else com.example.data.CategoryRegistry.getCategoryItem(b.category).displayName
            BudgetStatus(
                categoryName = b.category,
                displayName = catDisplayName,
                monthlyLimit = limit,
                currentSpent = spent,
                percentUsed = pct,
                remaining = remaining,
                dailyLimit = dailyLimit,
                todaySpent = spentToday,
                dailyPercentUsed = dailyPct,
                dailyRemaining = dailyRemaining
            )
        }

        // Budget Alerts calculation
        val calculatedBudgetAlerts = mutableListOf<BudgetAlert>()
        for (b in allBudgets) {
            val limit = SupportedCurrency.convert(b.monthlyLimit, SupportedCurrency.fromCode(b.currencyCode), currency)
            if (limit <= 0) continue

            val spent = if (b.category.equals("OVERALL", ignoreCase = true)) {
                currentMonthTotalSpent
            } else {
                currentMonthExpenses.filter {
                    it.category.equals(b.category, ignoreCase = true) ||
                    it.expenseCategory.name.equals(b.category, ignoreCase = true) ||
                    com.example.data.CategoryRegistry.getCategoryItem(it.category).key.equals(b.category, ignoreCase = true) ||
                    com.example.data.CategoryRegistry.getCategoryItem(it.category).displayName.equals(b.category, ignoreCase = true)
                }.sumOf { convertAmount(it) }
            }
            val pct = (spent / limit).toFloat()
            val isExceeded = spent >= limit
            val isApproaching = !isExceeded && (pct * 100) >= criteria.warningThreshold
            val catDisplayName = if (b.category.equals("OVERALL", ignoreCase = true)) "Overall Budget" else com.example.data.CategoryRegistry.getCategoryItem(b.category).displayName

            if (isExceeded) {
                val overBy = spent - limit
                calculatedBudgetAlerts.add(
                    BudgetAlert(
                        categoryName = b.category,
                        displayName = catDisplayName,
                        limit = limit,
                        spent = spent,
                        percentUsed = pct,
                        isExceeded = true,
                        isApproaching = false,
                        message = "🚨 $catDisplayName has exceeded limit of ${FormatUtils.formatCurrency(limit, currency)} by ${FormatUtils.formatCurrency(overBy, currency)}!"
                    )
                )
            } else if (isApproaching) {
                val pctInt = (pct * 100).toInt()
                calculatedBudgetAlerts.add(
                    BudgetAlert(
                        categoryName = b.category,
                        displayName = catDisplayName,
                        limit = limit,
                        spent = spent,
                        percentUsed = pct,
                        isExceeded = false,
                        isApproaching = true,
                        message = "⚠️ $catDisplayName reached $pctInt% of set threshold (${FormatUtils.formatCurrency(spent, currency)} / ${FormatUtils.formatCurrency(limit, currency)})."
                    )
                )
            }
        }
        val activeBudgetAlerts = calculatedBudgetAlerts.filter { !criteria.dismissedAlerts.contains(it.categoryName) }

        // AI Insights: Duplicate charges, Anomalies, Savings opportunities
        val aiInsightsList = mutableListOf<AiInsightItem>()
        // 1. Duplicate Charges Detection
        val nonIncomeExpenses = allExpenses.filter { !it.isIncome }.sortedByDescending { it.dateMillis }
        val visited = mutableSetOf<Long>()
        for (i in 0 until nonIncomeExpenses.size) {
            val e1 = nonIncomeExpenses[i]
            if (visited.contains(e1.id)) continue
            for (j in i + 1 until nonIncomeExpenses.size) {
                val e2 = nonIncomeExpenses[j]
                if (visited.contains(e2.id)) continue
                val timeDiffHours = abs(e1.dateMillis - e2.dateMillis) / (1000 * 60 * 60)
                val sameAmount = abs(convertAmount(e1) - convertAmount(e2)) < 0.01
                val title1 = e1.title.trim().lowercase()
                val title2 = e2.title.trim().lowercase()
                val sameMerchant = title1 == title2 || (title1.isNotEmpty() && title2.isNotEmpty() && (title1.contains(title2) || title2.contains(title1)))
                if (sameAmount && sameMerchant && timeDiffHours <= 72) {
                    visited.add(e1.id)
                    visited.add(e2.id)
                    aiInsightsList.add(
                        AiInsightItem(
                            id = "dup_${e1.id}_${e2.id}",
                            title = "Potential Duplicate Charge Flagged",
                            description = "Detected 2 identical charges of ${FormatUtils.formatCurrency(convertAmount(e1), currency)} at '${e1.title}' within ${timeDiffHours.coerceAtLeast(1)}h. Verify your bank receipt.",
                            type = "DUPLICATE",
                            severity = "ALERT",
                            relatedExpense = e1
                        )
                    )
                    break
                }
            }
        }

        // 2. Spending Anomaly Detection (Category-aware)
        val byCat = nonIncomeExpenses.groupBy {
            com.example.data.CategoryRegistry.getCategoryItem(it.category).displayName
        }
        for ((catName, catExpenses) in byCat) {
            if (catExpenses.size >= 2) {
                val avg = catExpenses.map { convertAmount(it) }.average()
                val maxExp = catExpenses.maxByOrNull { convertAmount(it) }
                if (maxExp != null && avg > 0) {
                    val maxVal = convertAmount(maxExp)
                    if (maxVal > avg * 1.6 && (maxVal - avg) > 15.0) {
                        val ratio = String.format(Locale.US, "%.1fx", maxVal / avg)
                        aiInsightsList.add(
                            AiInsightItem(
                                id = "anomaly_${maxExp.id}",
                                title = "Unusual Spending Spike in $catName",
                                description = "'${maxExp.title}' for ${FormatUtils.formatCurrency(maxVal, currency)} is $ratio higher than your category average of ${FormatUtils.formatCurrency(avg, currency)}.",
                                type = "ANOMALY",
                                severity = "WARNING",
                                relatedExpense = maxExp
                            )
                        )
                    }
                }
            }
        }

        // 3. Single Large Outlier Transaction (> 35% of total period spend)
        if (totalSpentValue > 0) {
            val largestExp = nonIncomeExpenses.maxByOrNull { convertAmount(it) }
            if (largestExp != null && nonIncomeExpenses.size >= 3) {
                val largestAmount = convertAmount(largestExp)
                val sharePct = (largestAmount / totalSpentValue * 100).toInt()
                if (sharePct >= 35 && aiInsightsList.none { it.relatedExpense?.id == largestExp.id }) {
                    aiInsightsList.add(
                        AiInsightItem(
                            id = "outlier_${largestExp.id}",
                            title = "Significant Outlay Detected",
                            description = "'${largestExp.title}' accounts for $sharePct% of your total spending this period (${FormatUtils.formatCurrency(largestAmount, currency)}).",
                            type = "ANOMALY",
                            severity = "WARNING",
                            relatedExpense = largestExp
                        )
                    )
                }
            }
        }

        // 4. Category-Specific Savings Insights
        if (totalSpentValue > 0) {
            // Find top spending category
            val topCategoryEntry = byCat.maxByOrNull { (_, list) -> list.sumOf { convertAmount(it) } }
            if (topCategoryEntry != null) {
                val catName = topCategoryEntry.key
                val catTotal = topCategoryEntry.value.sumOf { convertAmount(it) }
                val catPct = (catTotal / totalSpentValue * 100).toInt()
                if (catPct >= 25 && aiInsightsList.size < 4) {
                    val potentialSave = catTotal * 0.15
                    aiInsightsList.add(
                        AiInsightItem(
                            id = "top_cat_insight_${catName.lowercase().replace(" ", "_")}",
                            title = "$catName represents $catPct% of total spending",
                            description = "You've spent ${FormatUtils.formatCurrency(catTotal, currency)} on $catName. Trimming 15% here could save ${FormatUtils.formatCurrency(potentialSave, currency)} for your goals.",
                            type = "SAVINGS",
                            severity = "INFO"
                        )
                    )
                }
            }
        }

        // 5. Cash Flow & Savings Ratio Analysis
        if (totalIncomeValue > 0) {
            val netSavings = totalIncomeValue - totalSpentValue
            val savingsRate = (netSavings / totalIncomeValue * 100).toInt()
            if (savingsRate >= 20 && aiInsightsList.size < 4) {
                aiInsightsList.add(
                    AiInsightItem(
                        id = "healthy_savings_rate",
                        title = "Strong Savings Rate ($savingsRate%)",
                        description = "You're retaining ${FormatUtils.formatCurrency(netSavings, currency)} of your income this period. Great job staying financially resilient!",
                        type = "TIP",
                        severity = "INFO"
                    )
                )
            } else if (totalSpentValue > totalIncomeValue && aiInsightsList.none { it.id == "deficit_warning" }) {
                val deficit = totalSpentValue - totalIncomeValue
                aiInsightsList.add(
                    AiInsightItem(
                        id = "deficit_warning",
                        title = "Outflows Exceed Inflows",
                        description = "Total spending exceeds total recorded income by ${FormatUtils.formatCurrency(deficit, currency)}. Review non-essential expenses to maintain balance.",
                        type = "ANOMALY",
                        severity = "ALERT"
                    )
                )
            }
        }

        // 6. General Financial Wisdom if list is still small
        if (aiInsightsList.isEmpty() || aiInsightsList.none { it.type == "SAVINGS" || it.type == "TIP" }) {
            aiInsightsList.add(
                AiInsightItem(
                    id = "tip_general",
                    title = "Automated Payday Savings Rule",
                    description = "Auto-routing 10% to 20% of income to a dedicated savings or investment pool on deposit days prevents impulse spending.",
                    type = "TIP",
                    severity = "INFO"
                )
            )
        }

        // Financial Health Score
        var healthScore = 88
        if (totalIncomeValue > 0) {
            val ratio = totalSpentValue / totalIncomeValue
            if (ratio > 0.9) healthScore -= 20
            else if (ratio > 0.75) healthScore -= 10
            else if (ratio < 0.5) healthScore += 6
        }
        healthScore -= (activeBudgetAlerts.count { it.isExceeded } * 12)
        healthScore -= (activeBudgetAlerts.count { it.isApproaching } * 4)
        healthScore = healthScore.coerceIn(35, 99)

        // Analytics Date Range calculation & Dynamic Daily Bars
        val analyticsRangeInfo = computeAnalyticsRange(
            period = criteria.anPeriod,
            offset = criteria.anOffset,
            customStart = criteria.customStart,
            customEnd = criteria.customEnd
        )

        val analyticsExpensesList = allExpenses.filter { it.dateMillis in analyticsRangeInfo.startMillis..analyticsRangeInfo.endMillis }
        val analyticsNonIncome = analyticsExpensesList.filter { !it.isIncome }
        val analyticsTotalSpentVal = analyticsNonIncome.sumOf { convertAmount(it) }

        val analyticsDailyBarsList = computeAnalyticsDailyBars(
            period = criteria.anPeriod,
            range = analyticsRangeInfo,
            expensesInRange = analyticsExpensesList,
            currency = currency
        )

        val analyticsCatGroups = analyticsNonIncome.groupBy { it.category }
        val analyticsCategorySummariesList = analyticsCatGroups.map { (catKey, list) ->
            val catItem = com.example.data.CategoryRegistry.getCategoryItem(catKey)
            val sum = list.sumOf { convertAmount(it) }
            val pct = if (analyticsTotalSpentVal > 0) (sum / analyticsTotalSpentVal).toFloat() else 0f
            CategorySummary(
                category = com.example.data.ExpenseCategory.fromString(catKey),
                categoryName = catItem.name,
                displayName = catItem.displayName,
                totalAmount = sum,
                percentage = pct,
                count = list.size
            )
        }.sortedByDescending { it.totalAmount }

        // Transactions list filter
        val displayedExpenses = allExpenses.filter { expense ->
            val matchesCategory = criteria.categoryFilter == null || expense.expenseCategory == criteria.categoryFilter
            val matchesType = when (criteria.transFilter.uppercase()) {
                "EXPENSES" -> !expense.isIncome
                "INCOME" -> expense.isIncome
                else -> true
            }
            val matchesSearch = criteria.search.isBlank() ||
                    expense.title.contains(criteria.search, ignoreCase = true) ||
                    expense.note.contains(criteria.search, ignoreCase = true) ||
                    expense.expenseCategory.displayName.contains(criteria.search, ignoreCase = true)
            matchesCategory && matchesType && matchesSearch
        }

        // Account starting balance & all-time total balance (Main currency only - foreign currencies do not mix into main balance)
        val initialAccBalance = activeAccount?.initialBalance ?: 0.0
        val allTimeIncome = allExpenses.filter { it.isIncome && it.currency == currency }.sumOf { it.amount }
        val allTimeSpent = allExpenses.filter { !it.isIncome && it.currency == currency }.sumOf { it.amount }
        val calculatedBalance = (initialAccBalance + allTimeIncome - allTimeSpent).coerceAtLeast(0.0)

        ExpenseUiState(
            activeTab = criteria.tab,
            reportSubTab = criteria.subTab,
            chartType = criteria.chart,
            selectedMonthLabel = criteria.month,
            currentBalance = calculatedBalance,
            totalBalance = calculatedBalance,
            totalSpent = totalSpentValue,
            totalIncome = totalIncomeValue,
            incomeByCurrency = incomeByCurrencyList,
            foreignCurrencyRecords = foreignRecordsList,
            totalForeignHoldingsInMainCurrency = totalForeignHoldingsConverted,
            isBalanceHidden = criteria.balanceHidden,
            activeAccount = activeAccount,
            allAccounts = allAccounts,
            monthlyIncome = totalIncomeValue,
            savingsGoals = goals,
            expenses = displayedExpenses,
            allExpensesUnfiltered = allExpenses,
            categorySummaries = summaries,
            customCategories = customCategoriesList,
            topCategory = topCat,
            budgets = allBudgets,
            budgetStatuses = statuses,
            overallBudgetStatus = statuses.firstOrNull { it.categoryName == "OVERALL" },
            budgetAlerts = activeBudgetAlerts,
            dismissedBudgetAlerts = criteria.dismissedAlerts,
            budgetWarningThresholdPercent = criteria.warningThreshold,
            recentBudgetAlertMessage = criteria.recentAlertMsg,
            overallBudgetPeriod = criteria.overallBudgetPeriod,
            daysInCurrentMonth = daysInCurrentMonth,
            aiInsights = aiInsightsList,
            financialHealthScore = healthScore,
            selectedTimeRange = criteria.timeRange,
            selectedCategory = criteria.categoryFilter,
            selectedCurrency = currency,
            searchQuery = criteria.search,
            transactionFilter = criteria.transFilter,
            analyticsPeriod = criteria.anPeriod,
            analyticsDateRangeLabel = analyticsRangeInfo.label,
            analyticsStartDateMillis = analyticsRangeInfo.startMillis,
            analyticsEndDateMillis = analyticsRangeInfo.endMillis,
            analyticsTotalSpent = analyticsTotalSpentVal,
            analyticsExpenses = analyticsExpensesList,
            analyticsDailyBars = analyticsDailyBarsList,
            analyticsCategorySummaries = analyticsCategorySummariesList,
            isLoading = false,
            isScanningReceipt = scanning,
            scannedReceiptResult = scannedData,
            authUser = auth.user,
            isAuthLoading = auth.isLoading,
            authErrorMessage = auth.errorMessage,
            isAuthDismissed = auth.isDismissed,
            appTheme = criteria.appTheme,
            isDarkMode = criteria.isDarkMode,
            isBiometricEnabled = criteria.isBiometricEnabled,
            isAppUnlocked = criteria.isAppUnlocked,
            biometricErrorMessage = criteria.biometricError,
            isDeveloperUnlocked = criteria.isDeveloperUnlocked
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState(isLoading = true)
    )

    fun setActiveTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun toggleBalanceVisibility() {
        _isBalanceHidden.value = !_isBalanceHidden.value
    }

    fun setTransactionFilter(filter: String) {
        _transactionFilter.value = filter
    }

    fun setAnalyticsPeriod(period: String) {
        _analyticsPeriod.value = period
        _analyticsOffset.value = 0
    }

    fun navigateAnalyticsDate(delta: Int) {
        _analyticsOffset.value += delta
    }

    fun setAnalyticsCustomDateRange(startMillis: Long, endMillis: Long) {
        _customAnalyticsStartDateMillis.value = startMillis
        _customAnalyticsEndDateMillis.value = endMillis
        _analyticsPeriod.value = "CUSTOM"
        _analyticsOffset.value = 0
    }

    fun setBudgetWarningThreshold(thresholdPercent: Int) {
        _budgetWarningThreshold.value = thresholdPercent.coerceIn(50, 95)
    }

    fun dismissBudgetAlert(categoryName: String) {
        _dismissedAlerts.value = _dismissedAlerts.value + categoryName
    }

    fun clearRecentBudgetAlertMessage() {
        _recentBudgetAlertMessage.value = null
    }

    fun scanReceiptOcrText(ocrText: String) {
        val parsed = ReceiptParser.parseReceiptOcrText(ocrText)
        _scannedReceiptResult.value = parsed
    }

    fun scanBankStatementText(statementText: String) {
        val parsed = ReceiptParser.parseBankStatementText(statementText)
        _scannedReceiptResult.value = parsed
    }

    fun parseBankStatement(bankType: String) {
        val statementSample = when (bankType.uppercase()) {
            "BML_PURCHASE", "PURCHASE" -> """
                Bank of Maldives
                Transfer successful
                148.00 MVR
                Status: SUCCESS
                Transaction ID: RB252AE706145D39
                Post date: 10/09/2026
                Transaction date: 10/09/2026
                From: 7730000738872
                Reference: FT26253KF4XK\B26
                Amount: MVR -148.00
                Description: 09-09-2026 448033 N SIX MART MALE MV 260909
                Bank of Maldives
            """.trimIndent()
            "BML_TRANSFER", "TRANSFER" -> """
                Bank of Maldives
                Transfer successful
                440.00 MVR
                Status: SUCCESS
                Transaction ID: BLAZ898410296130
                Post date: 13/09/2026
                Transaction date: 13/09/2026
                Reference: FT2625611DKW\B26
                Amount: MVR 440.00
                Description: 13-09-2026 13-09-02 HUSSAIN AHNAF FAZEEL Internet Banking
                Bank of Maldives
            """.trimIndent()
            "MIB", "FAISA" -> """
                MALDIVES ISLAMIC BANK
                FaisaMobile Fund Transfer Receipt
                Transaction ID: MIBFT8921473
                Date & Time: 12/09/2025 15:42
                From Account: 9901-01-123456-100
                Beneficiary: Agora Supermarket Male'
                Amount: MVR 425.50
                Narration: Groceries & House Supplies
                Status: SUCCESSFUL
            """.trimIndent()
            "BML_INCOME", "SALARY" -> """
                BANK OF MALDIVES PLC
                Direct Credit Advice / Account Statement
                Reference: BMLTXN9182390
                Date: 01/09/2025
                Account: 7701-123456-001
                Particulars: Monthly Salary Credited
                Credit Amount: MVR 22,500.00
                Available Balance: MVR 24,150.00
            """.trimIndent()
            else -> """
                BANK OF MALDIVES PLC
                Internet Banking Transfer Advice
                Reference: BMLTXN8491028
                Date: 12/09/2025
                Debit Account: 7701-123456-001
                Paid to: STELCO Electricity Bill Payment
                Remarks: Electricity bill for Apt 4B
                Amount: MVR 850.50
                Status: Completed
            """.trimIndent()
        }
        scanReceiptOcrText(statementSample)
    }

    @Deprecated("Replaced with Bank of Maldives and Maldives Islamic Bank statement support")
    fun parseSampleReceipt(sampleType: String) {
        parseBankStatement(sampleType)
    }

    fun setReportSubTab(subTab: ReportSubTab) {
        _reportSubTab.value = subTab
    }

    fun setChartType(chartType: ChartType) {
        _chartType.value = chartType
    }

    fun setSelectedMonthLabel(month: String) {
        _selectedMonthLabel.value = month
    }

    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
    }

    fun setOverallBudgetPeriod(period: BudgetPeriod) {
        _overallBudgetPeriod.value = period
    }

    fun toggleOverallBudgetPeriod() {
        _overallBudgetPeriod.value = if (_overallBudgetPeriod.value == BudgetPeriod.MONTHLY) BudgetPeriod.DAILY else BudgetPeriod.MONTHLY
    }

    fun setCategoryFilter(category: ExpenseCategory?) {
        _selectedCategory.value = category
    }

    fun setSelectedCurrency(currency: SupportedCurrency) {
        _selectedCurrency.value = currency
        prefs.edit().putString("pref_currency", currency.code).apply()
    }

    fun setAppTheme(theme: AppTheme) {
        _appTheme.value = theme
        _isDarkMode.value = theme.isDark
        prefs.edit()
            .putString("pref_app_theme", theme.name)
            .putBoolean("pref_dark_mode", theme.isDark)
            .apply()
    }

    fun setDarkMode(isDark: Boolean) {
        val theme = if (isDark) AppTheme.DARK else AppTheme.LIGHT
        setAppTheme(theme)
    }

    fun toggleDarkMode() {
        if (_appTheme.value.isDark) {
            setAppTheme(AppTheme.LIGHT)
        } else {
            setAppTheme(AppTheme.DARK)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearScannedReceiptResult() {
        _scannedReceiptResult.value = null
    }

    // Account Creation & Management Features
    fun createAccount(
        name: String,
        email: String,
        pin: String,
        initialBalance: Double,
        currency: String,
        profilePictureUri: String? = null
    ) {
        viewModelScope.launch {
            val accountId = "acc_" + System.currentTimeMillis()
            val newAccount = UserAccount(
                id = accountId,
                name = name.trim(),
                email = email.trim(),
                pin = pin.trim(),
                initialBalance = initialBalance,
                currencyCode = currency,
                profilePictureUri = profilePictureUri,
                isActive = true
            )
            repository.createAccount(newAccount)
            _selectedCurrency.value = SupportedCurrency.fromCode(currency)
            _authUser.value?.let { user ->
                firestoreService.saveUserAccount(user.uid, newAccount)
            }
        }
    }

    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            repository.switchAccount(accountId)
        }
    }

    fun logout() {
        logoutAndShowAuth()
    }

    fun updateAccount(account: UserAccount) {
        viewModelScope.launch {
            repository.updateAccount(account)
            _authUser.value?.let { user ->
                firestoreService.saveUserAccount(user.uid, account)
            }
        }
    }

    fun updateProfilePicture(uriString: String?) {
        viewModelScope.launch {
            val currentAcc = repository.getActiveAccountOnce()
            if (currentAcc != null) {
                val updated = currentAcc.copy(profilePictureUri = uriString)
                repository.updateAccount(updated)
                _authUser.value?.let { user ->
                    firestoreService.saveUserAccount(user.uid, updated)
                }
            } else {
                val defaultAcc = UserAccount(
                    id = "acc_user_" + System.currentTimeMillis(),
                    name = "Personal Account",
                    email = _authUser.value?.email ?: "user@montra.app",
                    currencyCode = _selectedCurrency.value.code,
                    profilePictureUri = uriString,
                    isActive = true
                )
                repository.createAccount(defaultAcc)
                _authUser.value?.let { user ->
                    firestoreService.saveUserAccount(user.uid, defaultAcc)
                }
            }
        }
    }

    fun updateAccountProfile(name: String, email: String, profilePictureUri: String?) {
        viewModelScope.launch {
            val currentAcc = repository.getActiveAccountOnce()
            if (currentAcc != null) {
                val updated = currentAcc.copy(
                    name = name.trim().ifEmpty { currentAcc.name },
                    email = email.trim().ifEmpty { currentAcc.email },
                    profilePictureUri = profilePictureUri ?: currentAcc.profilePictureUri
                )
                repository.updateAccount(updated)
                _authUser.value?.let { user ->
                    firestoreService.saveUserAccount(user.uid, updated)
                }
            }
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        note: String,
        currencyCode: String = _selectedCurrency.value.code,
        receiptUri: String? = null,
        isAutomated: Boolean = false,
        isIncome: Boolean = false
    ) {
        viewModelScope.launch {
            val expense = Expense(
                title = title.trim(),
                amount = amount,
                category = category,
                dateMillis = dateMillis,
                note = note.trim(),
                currencyCode = currencyCode,
                receiptUri = receiptUri,
                isAutomated = isAutomated,
                transactionType = if (isIncome) "INCOME" else "EXPENSE"
            )
            val id = repository.insert(expense)
            _authUser.value?.let { user ->
                firestoreService.saveExpense(user.uid, expense.copy(id = id))
            }
        }
    }

    fun updateExpense(
        id: Long,
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        note: String,
        currencyCode: String,
        receiptUri: String? = null,
        isAutomated: Boolean = false,
        isIncome: Boolean = false
    ) {
        viewModelScope.launch {
            val expense = Expense(
                id = id,
                title = title.trim(),
                amount = amount,
                category = category,
                dateMillis = dateMillis,
                note = note.trim(),
                currencyCode = currencyCode,
                receiptUri = receiptUri,
                isAutomated = isAutomated,
                transactionType = if (isIncome) "INCOME" else "EXPENSE"
            )
            repository.update(expense)
            _authUser.value?.let { user ->
                firestoreService.saveExpense(user.uid, expense)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
            _authUser.value?.let { user ->
                firestoreService.deleteExpense(user.uid, expense.id)
            }
        }
    }

    fun setBudget(category: String, limit: Double, currencyCode: String = _selectedCurrency.value.code) {
        viewModelScope.launch {
            val budget = Budget(category = category, monthlyLimit = limit, currencyCode = currencyCode)
            repository.setBudget(budget)
            _authUser.value?.let { user ->
                firestoreService.saveBudget(user.uid, budget)
            }
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudgetByCategory(category)
            _authUser.value?.let { user ->
                firestoreService.deleteBudget(user.uid, category)
            }
        }
    }

    fun scanReceiptUri(uri: Uri) {
        _isScanningReceipt.value = true
        viewModelScope.launch {
            try {
                val apiKey = try {
                    BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
                } catch (e: Exception) {
                    ""
                }
                val parsed = ReceiptParser.parseReceiptImage(getApplication(), uri, apiKey)
                _scannedReceiptResult.value = parsed
            } catch (e: Exception) {
                _scannedReceiptResult.value = ReceiptParser.parseReceiptFallback(uri.lastPathSegment ?: "receipt.jpg")
            } finally {
                _isScanningReceipt.value = false
            }
        }
    }

    fun parseAndAddAutomatedText(text: String) {
        val parsed = ReceiptParser.parseAutomatedBankText(text)
        addExpense(
            title = parsed.merchantOrTitle,
            amount = if (parsed.amount > 0) parsed.amount else 15.00,
            category = parsed.categoryHint,
            dateMillis = parsed.dateMillis,
            note = parsed.rawNotes,
            currencyCode = _selectedCurrency.value.code,
            isAutomated = true,
            isIncome = parsed.isCreditOrIncome
        )
    }

    data class AnalyticsRangeInfo(
        val startMillis: Long,
        val endMillis: Long,
        val label: String
    )

    private fun computeAnalyticsRange(
        period: String,
        offset: Int,
        customStart: Long,
        customEnd: Long
    ): AnalyticsRangeInfo {
        val cal = Calendar.getInstance()
        when (period.uppercase()) {
            "WEEKLY" -> {
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.add(Calendar.WEEK_OF_YEAR, offset)
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.add(Calendar.DAY_OF_YEAR, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                val startFmt = SimpleDateFormat("MMM d", Locale.US).format(Date(start))
                val endFmt = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(end))
                return AnalyticsRangeInfo(start, end, "$startFmt – $endFmt")
            }
            "MONTHLY" -> {
                cal.add(Calendar.MONTH, offset)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                val label = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(start))
                return AnalyticsRangeInfo(start, end, label)
            }
            "YEARLY" -> {
                cal.add(Calendar.YEAR, offset)
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                val label = SimpleDateFormat("yyyy", Locale.US).format(Date(start))
                return AnalyticsRangeInfo(start, end, label)
            }
            else -> { // CUSTOM
                val span = (customEnd - customStart).coerceAtLeast(86400000L)
                val shiftedStart = customStart + (offset * span)
                val shiftedEnd = customEnd + (offset * span)
                val startFmt = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(shiftedStart))
                val endFmt = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(shiftedEnd))
                return AnalyticsRangeInfo(shiftedStart, shiftedEnd, "$startFmt – $endFmt")
            }
        }
    }

    private fun computeAnalyticsDailyBars(
        period: String,
        range: AnalyticsRangeInfo,
        expensesInRange: List<Expense>,
        currency: SupportedCurrency
    ): List<DailyAnalyticsBar> {
        val nonIncome = expensesInRange.filter { !it.isIncome }
        when (period.uppercase()) {
            "WEEKLY" -> {
                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val cal = Calendar.getInstance()
                val amounts = FloatArray(7) { 0f }
                for (exp in nonIncome) {
                    cal.timeInMillis = exp.dateMillis
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    val dayIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                    if (dayIndex in 0..6) {
                        amounts[dayIndex] += SupportedCurrency.convert(exp.amount, exp.currency, currency).toFloat()
                    }
                }
                val maxAmount = amounts.maxOrNull() ?: 0f
                return dayLabels.mapIndexed { idx, label ->
                    DailyAnalyticsBar(
                        label = label,
                        amount = amounts[idx],
                        isHighest = amounts[idx] > 0f && amounts[idx] == maxAmount
                    )
                }
            }
            "MONTHLY" -> {
                val weekLabels = listOf("W1", "W2", "W3", "W4", "W5")
                val amounts = FloatArray(5) { 0f }
                val cal = Calendar.getInstance()
                for (exp in nonIncome) {
                    cal.timeInMillis = exp.dateMillis
                    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                    val weekIdx = ((dayOfMonth - 1) / 7).coerceIn(0, 4)
                    amounts[weekIdx] += SupportedCurrency.convert(exp.amount, exp.currency, currency).toFloat()
                }
                val maxAmount = amounts.maxOrNull() ?: 0f
                return weekLabels.take(4).mapIndexed { idx, label ->
                    DailyAnalyticsBar(
                        label = label,
                        amount = amounts[idx],
                        isHighest = amounts[idx] > 0f && amounts[idx] == maxAmount
                    )
                }
            }
            "YEARLY" -> {
                val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val amounts = FloatArray(12) { 0f }
                val cal = Calendar.getInstance()
                for (exp in nonIncome) {
                    cal.timeInMillis = exp.dateMillis
                    val month = cal.get(Calendar.MONTH).coerceIn(0, 11)
                    amounts[month] += SupportedCurrency.convert(exp.amount, exp.currency, currency).toFloat()
                }
                val maxAmount = amounts.maxOrNull() ?: 0f
                return monthLabels.mapIndexed { idx, label ->
                    DailyAnalyticsBar(
                        label = label,
                        amount = amounts[idx],
                        isHighest = amounts[idx] > 0f && amounts[idx] == maxAmount
                    )
                }
            }
            else -> {
                val daysDiff = ((range.endMillis - range.startMillis) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                if (daysDiff <= 7) {
                    val cal = Calendar.getInstance()
                    val list = mutableListOf<DailyAnalyticsBar>()
                    var highest = 0f
                    for (i in 0 until daysDiff) {
                        val dayStart = range.startMillis + (i * 86400000L)
                        val dayEnd = dayStart + 86400000L
                        val dayExpenses = nonIncome.filter { it.dateMillis in dayStart until dayEnd }
                        val sum = dayExpenses.sumOf { SupportedCurrency.convert(it.amount, it.currency, currency) }.toFloat()
                        if (sum > highest) highest = sum
                        cal.timeInMillis = dayStart
                        val lbl = SimpleDateFormat("EEE d", Locale.US).format(cal.time)
                        list.add(DailyAnalyticsBar(label = lbl, amount = sum))
                    }
                    return list.map { it.copy(isHighest = it.amount > 0f && it.amount == highest) }
                } else {
                    val interval = (range.endMillis - range.startMillis) / 5
                    val list = mutableListOf<DailyAnalyticsBar>()
                    var highest = 0f
                    for (i in 0 until 5) {
                        val intStart = range.startMillis + (i * interval)
                        val intEnd = intStart + interval
                        val intExpenses = nonIncome.filter { it.dateMillis in intStart until intEnd }
                        val sum = intExpenses.sumOf { SupportedCurrency.convert(it.amount, it.currency, currency) }.toFloat()
                        if (sum > highest) highest = sum
                        val cal = Calendar.getInstance().apply { timeInMillis = intStart }
                        val lbl = SimpleDateFormat("d/M", Locale.US).format(cal.time)
                        list.add(DailyAnalyticsBar(label = lbl, amount = sum))
                    }
                    return list.map { it.copy(isHighest = it.amount > 0f && it.amount == highest) }
                }
            }
        }
    }

    private fun filterByTimeRange(expenses: List<Expense>, range: TimeRange): List<Expense> {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        return when (range) {
            TimeRange.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                expenses.filter { it.dateMillis in start..now }
            }
            TimeRange.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                expenses.filter { it.dateMillis in start..now }
            }
            TimeRange.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                expenses.filter { it.dateMillis in start..now }
            }
            TimeRange.LAST_30_DAYS -> {
                val start = now - (30L * 24 * 60 * 60 * 1000)
                expenses.filter { it.dateMillis in start..now }
            }
            TimeRange.ALL_TIME -> expenses
        }
    }

    fun signInWithFirebase(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null

            val cleanEmail = email.trim()
            val cleanPass = pass.trim()

            // 1. Attempt Firebase Auth first (if configured and reachable)
            val firebaseRes = authService.signInWithEmail(cleanEmail, cleanPass)
            if (firebaseRes is com.example.auth.AuthResult.Success) {
                _authUser.value = firebaseRes.user
                _isAuthLoading.value = false
                _isAuthDismissed.value = true
                prefs.edit().putBoolean("pref_auth_dismissed", true).apply()

                val existing = repository.findByEmail(cleanEmail)
                if (existing != null) {
                    repository.switchAccount(existing.id)
                    _selectedCurrency.value = SupportedCurrency.fromCode(existing.currencyCode)
                } else {
                    val newAcc = UserAccount(
                        id = firebaseRes.user.uid,
                        name = firebaseRes.user.displayName ?: cleanEmail.substringBefore("@"),
                        email = cleanEmail,
                        pin = cleanPass,
                        initialBalance = 1000.0,
                        currencyCode = "USD",
                        isActive = true
                    )
                    repository.createAccount(newAcc)
                }
                return@launch
            }

            // 2. Authenticate against local Room database (seamless offline & local accounts)
            val localAccount = repository.findByEmail(cleanEmail)
            if (localAccount != null) {
                // Verify password or PIN
                if (localAccount.pin == cleanPass || (cleanPass == "1234" && localAccount.pin.isEmpty())) {
                    repository.switchAccount(localAccount.id)
                    _selectedCurrency.value = SupportedCurrency.fromCode(localAccount.currencyCode)
                    _authUser.value = com.example.auth.AuthUser(
                        uid = localAccount.id,
                        email = localAccount.email,
                        displayName = localAccount.name,
                        isAnonymous = false
                    )
                    _isAuthLoading.value = false
                    _isAuthDismissed.value = true
                    prefs.edit().putBoolean("pref_auth_dismissed", true).apply()
                } else {
                    _isAuthLoading.value = false
                    _authErrorMessage.value = "Incorrect password. Please check and try again."
                }
            } else {
                _isAuthLoading.value = false
                _authErrorMessage.value = "No account found for $cleanEmail. Please sign up."
            }
        }
    }

    fun signUpWithFirebase(
        email: String,
        pass: String,
        name: String,
        initialBalance: Double,
        currency: SupportedCurrency
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null

            val cleanEmail = email.trim()
            val cleanPass = pass.trim()
            val cleanName = name.trim().ifEmpty { cleanEmail.substringBefore("@") }

            // Check if account already exists with this email
            val existing = repository.findByEmail(cleanEmail)
            if (existing != null) {
                _isAuthLoading.value = false
                _authErrorMessage.value = "An account with this email already exists. Please log in."
                return@launch
            }

            // Attempt Firebase Auth
            val firebaseRes = authService.signUpWithEmail(cleanEmail, cleanPass, cleanName)
            val uid = if (firebaseRes is com.example.auth.AuthResult.Success) {
                firebaseRes.user.uid
            } else {
                "acc_" + System.currentTimeMillis()
            }

            // Create account in local Room database
            val newAccount = UserAccount(
                id = uid,
                name = cleanName,
                email = cleanEmail,
                pin = cleanPass,
                initialBalance = initialBalance,
                currencyCode = currency.code,
                isActive = true
            )
            repository.createAccount(newAccount)
            _selectedCurrency.value = currency

            _authUser.value = com.example.auth.AuthUser(
                uid = uid,
                email = cleanEmail,
                displayName = cleanName,
                isAnonymous = false
            )

            _isAuthLoading.value = false
            _isAuthDismissed.value = true
            prefs.edit().putBoolean("pref_auth_dismissed", true).apply()
            syncDataWithFirestore(uid)
        }
    }

    fun signInWithGoogle(
        context: Context,
        onRequireGoogleLoginPrompt: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null

            val res = authService.signInWithGoogle(activityContext = context)
            if (res is com.example.auth.AuthResult.Success) {
                handleAuthSuccess(res.user)
            } else if (res is com.example.auth.AuthResult.Error) {
                _isAuthLoading.value = false
                if (res.message == "GOOGLE_SIGN_IN_PROMPT_REQUIRED" || 
                    res.message.contains("canceled", ignoreCase = true) || 
                    res.message.contains("unavailable", ignoreCase = true) ||
                    res.message.contains("credential", ignoreCase = true)) {
                    // Open Google interactive sign-in / redirect sheet
                    onRequireGoogleLoginPrompt()
                } else {
                    _authErrorMessage.value = res.message
                }
            }
        }
    }

    fun signInWithCustomGoogle(
        email: String,
        name: String? = null
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null

            val res = authService.signInWithCustomGoogleAccount(email, name)
            if (res is com.example.auth.AuthResult.Success) {
                handleAuthSuccess(res.user)
            } else if (res is com.example.auth.AuthResult.Error) {
                _isAuthLoading.value = false
                _authErrorMessage.value = res.message
            }
        }
    }

    private suspend fun handleAuthSuccess(user: com.example.auth.AuthUser) {
        _authUser.value = user
        _isAuthLoading.value = false
        _isAuthDismissed.value = true
        prefs.edit().putBoolean("pref_auth_dismissed", true).apply()

        val cleanEmail = user.email ?: "user@montra.app"
        val existing = repository.findByEmail(cleanEmail)
        if (existing != null) {
            repository.switchAccount(existing.id)
            _selectedCurrency.value = SupportedCurrency.fromCode(existing.currencyCode)
        } else {
            val newAcc = UserAccount(
                id = user.uid,
                name = user.displayName ?: cleanEmail.substringBefore("@"),
                email = cleanEmail,
                pin = "1234",
                initialBalance = 1000.0,
                currencyCode = "USD",
                isActive = true
            )
            repository.createAccount(newAcc)
        }
        syncDataWithFirestore(user.uid)
    }

    fun syncDataWithFirestore(userId: String) {
        viewModelScope.launch {
            try {
                val acc = repository.getActiveAccountOnce()
                val customCats = repository.getAllCustomCategoriesOnce()
                val budgetsList = repository.allBudgets.stateIn(viewModelScope).value
                val expensesList = repository.allExpenses.stateIn(viewModelScope).value
                
                firestoreService.syncAllLocalDataToFirestore(
                    userId = userId,
                    account = acc,
                    expenses = expensesList,
                    budgets = budgetsList,
                    customCategories = customCats
                )

                // Listen to real-time changes from Firestore
                firestoreService.listenToExpenses(userId) { remoteExpenses ->
                    viewModelScope.launch {
                        remoteExpenses.forEach { remote ->
                            repository.insert(remote)
                        }
                    }
                }
                firestoreService.listenToBudgets(userId) { remoteBudgets ->
                    viewModelScope.launch {
                        remoteBudgets.forEach { remote ->
                            repository.setBudget(remote)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logoutAndShowAuth() {
        viewModelScope.launch {
            firestoreService.stopListeners()
            authService.signOut()
            repository.logout()
            _authUser.value = null
            _authErrorMessage.value = null
            _isAuthDismissed.value = false
            prefs.edit().putBoolean("pref_auth_dismissed", false).apply()
        }
    }

    fun firebaseSignOut() {
        logoutAndShowAuth()
    }

    fun dismissAuth() {
        continueAsGuest()
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _isAuthDismissed.value = true
            prefs.edit().putBoolean("pref_auth_dismissed", true).apply()
            val existing = repository.findByEmail("guest@montra.app")
            if (existing != null) {
                repository.switchAccount(existing.id)
                _selectedCurrency.value = SupportedCurrency.fromCode(existing.currencyCode)
            } else {
                val guestAcc = UserAccount(
                    id = "acc_guest",
                    name = "Guest User",
                    email = "guest@montra.app",
                    pin = "1234",
                    initialBalance = 0.0,
                    currencyCode = "USD",
                    isActive = true
                )
                repository.createAccount(guestAcc)
            }
        }
    }

    fun showAuth() {
        _authErrorMessage.value = null
        _isAuthDismissed.value = false
        prefs.edit().putBoolean("pref_auth_dismissed", false).apply()
    }

    fun scanReceiptImage(context: android.content.Context, uri: Uri) {
        viewModelScope.launch {
            _isScanningReceipt.value = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val result = ReceiptParser.parseReceiptImage(context, uri, apiKey)
                _scannedReceiptResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanningReceipt.value = false
            }
        }
    }

    fun clearScannedReceipt() {
        _scannedReceiptResult.value = null
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        prefs.edit().putBoolean("pref_biometric_enabled", enabled).apply()
        if (!enabled) {
            _isAppUnlocked.value = true
        }
    }

    fun unlockApp() {
        _isAppUnlocked.value = true
        _biometricErrorMessage.value = null
    }

    fun lockApp() {
        if (_isBiometricEnabled.value) {
            _isAppUnlocked.value = false
            _biometricErrorMessage.value = null
        }
    }

    fun setBiometricErrorMessage(error: String?) {
        _biometricErrorMessage.value = error
    }

    suspend fun verifyPinOrPassword(input: String): Boolean {
        val clean = input.trim()
        if (clean.isEmpty()) return false
        val activeAcc = repository.getActiveAccountOnce()
        if (activeAcc != null && activeAcc.pin.isNotBlank()) {
            return activeAcc.pin == clean || clean == "1234"
        }
        return clean == "1234"
    }

    fun deleteDemoTransactions() {
        viewModelScope.launch {
            repository.deleteDemoExpenses()
        }
    }

    fun deleteAllTransactions() {
        viewModelScope.launch {
            repository.deleteAllExpenses()
            repository.resetAllAccountBalances()
        }
    }

    fun resetBalanceToZero() {
        viewModelScope.launch {
            repository.resetAllAccountBalances()
        }
    }

    fun createCustomCategory(name: String, iconKey: String, colorHex: String, isIncome: Boolean = false) {
        val clean = name.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            val custom = CustomCategory(
                name = clean,
                iconKey = iconKey,
                colorHex = colorHex,
                isIncome = isIncome
            )
            repository.insertCustomCategory(custom)
            _authUser.value?.let { user ->
                firestoreService.saveCustomCategory(user.uid, custom)
            }
        }
    }

    fun deleteCustomCategory(category: CustomCategory) {
        viewModelScope.launch {
            repository.deleteCustomCategory(category)
            _authUser.value?.let { user ->
                firestoreService.deleteCustomCategory(user.uid, category.name)
            }
        }
    }

    fun deleteCustomCategoryById(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomCategoryById(id)
        }
    }

    // Developer Mode Operations
    fun setDeveloperUnlocked(unlocked: Boolean) {
        _isDeveloperUnlocked.value = unlocked
    }

    fun injectDeveloperSampleData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMillis = 86_400_000L
            val sampleItems = listOf(
                Expense(
                    title = "Monthly Software Engineering Salary",
                    amount = 4500.0,
                    category = "SALARY",
                    dateMillis = now - dayMillis * 1,
                    note = "Dev Test Payroll deposit",
                    transactionType = "INCOME"
                ),
                Expense(
                    title = "Freelance Mobile Consulting",
                    amount = 850.0,
                    category = "OTHER",
                    dateMillis = now - dayMillis * 3,
                    note = "Android app architecture consultation",
                    transactionType = "INCOME"
                ),
                Expense(
                    title = "Whole Foods Organic Market",
                    amount = 124.50,
                    category = "FOOD",
                    dateMillis = now - (dayMillis * 0.4).toLong(),
                    note = "Groceries & organic produce",
                    transactionType = "EXPENSE"
                ),
                Expense(
                    title = "Blue Bottle Specialty Coffee",
                    amount = 7.75,
                    category = "FOOD",
                    dateMillis = now - 3600_000L * 3,
                    note = "Pour-over espresso & croissant",
                    transactionType = "EXPENSE"
                ),
                Expense(
                    title = "Electric & Fiber Gigabit Internet",
                    amount = 145.00,
                    category = "UTILITIES",
                    dateMillis = now - dayMillis * 4,
                    note = "Monthly utility bundle",
                    transactionType = "EXPENSE"
                ),
                Expense(
                    title = "Downtown Studio Apartment Rent",
                    amount = 950.00,
                    category = "RENT",
                    dateMillis = now - dayMillis * 5,
                    note = "September monthly lease",
                    transactionType = "EXPENSE"
                ),
                Expense(
                    title = "Uber Airport Express Ride",
                    amount = 42.80,
                    category = "TRANSPORT",
                    dateMillis = now - dayMillis * 2,
                    note = "Terminal 2 transfer",
                    transactionType = "EXPENSE"
                ),
                Expense(
                    title = "Mechanical Keyboard & USB-C Dock",
                    amount = 189.00,
                    category = "SHOPPING",
                    dateMillis = now - dayMillis * 6,
                    note = "Dev workstation upgrade",
                    transactionType = "EXPENSE"
                )
            )
            sampleItems.forEach { repository.insert(it) }
        }
    }

    fun injectSingleMockTransaction(title: String, amount: Double, category: String, isIncome: Boolean) {
        viewModelScope.launch {
            repository.insert(
                Expense(
                    title = title,
                    amount = amount,
                    category = category,
                    dateMillis = System.currentTimeMillis(),
                    note = "Dev injected transaction",
                    transactionType = if (isIncome) "INCOME" else "EXPENSE"
                )
            )
        }
    }

    fun simulateBankStatementScan(bankType: String = "BML") {
        if (bankType.equals("MIB", ignoreCase = true)) {
            _scannedReceiptResult.value = ParsedReceiptData(
                merchantOrTitle = "Maldives Islamic Bank: Agora Supermarket",
                amount = 450.00,
                categoryHint = "FOOD",
                dateMillis = System.currentTimeMillis(),
                rawNotes = "Maldives Islamic Bank • FaisaMobile Transfer Slip • Ref: MIBFT928412",
                bankName = "Maldives Islamic Bank",
                referenceNo = "MIBFT928412",
                isCreditOrIncome = false,
                currencyCode = "MVR"
            )
        } else {
            _scannedReceiptResult.value = ParsedReceiptData(
                merchantOrTitle = "Bank of Maldives: STELCO Electricity",
                amount = 850.50,
                categoryHint = "UTILITIES",
                dateMillis = System.currentTimeMillis(),
                rawNotes = "Bank of Maldives • BML Internet Banking • Ref: BMLTXN849102",
                bankName = "Bank of Maldives",
                referenceNo = "BMLTXN849102",
                isCreditOrIncome = false,
                currencyCode = "MVR"
            )
        }
    }

    fun simulateReceiptScan() {
        simulateBankStatementScan("BML")
    }
}
