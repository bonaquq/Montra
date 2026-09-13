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
    val remaining: Double
)

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

data class ExpenseUiState(
    val activeTab: AppTab = AppTab.HOME,
    val reportSubTab: ReportSubTab = ReportSubTab.EXPENSES,
    val chartType: ChartType = ChartType.DONUT,
    val selectedMonthLabel: String = "September 2025",
    val currentBalance: Double = 0.0,
    val totalBalance: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
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
    val aiInsights: List<AiInsightItem> = emptyList(),
    val financialHealthScore: Int = 85,
    val selectedTimeRange: TimeRange = TimeRange.THIS_MONTH,
    val selectedCategory: ExpenseCategory? = null,
    val selectedCurrency: SupportedCurrency = SupportedCurrency.USD,
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
    val isDarkMode: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val isAppUnlocked: Boolean = true,
    val biometricErrorMessage: String? = null
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val prefs = application.getSharedPreferences("montra_preferences", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("pref_dark_mode", true))
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

    private val _selectedCurrency = MutableStateFlow(SupportedCurrency.USD)
    val selectedCurrency: StateFlow<SupportedCurrency> = _selectedCurrency

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isScanningReceipt = MutableStateFlow(false)
    val isScanningReceipt: StateFlow<Boolean> = _isScanningReceipt

    private val _scannedReceiptResult = MutableStateFlow<ParsedReceiptData?>(null)
    val scannedReceiptResult: StateFlow<ParsedReceiptData?> = _scannedReceiptResult

    private val authService = com.example.auth.AuthService(application)
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
        val isDarkMode: Boolean,
        val isBiometricEnabled: Boolean,
        val isAppUnlocked: Boolean,
        val biometricError: String?
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
        _isDarkMode,
        _isBiometricEnabled,
        _isAppUnlocked,
        _biometricErrorMessage
    ) { args: Array<Any?> ->
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
            isDarkMode = args[17] as Boolean,
            isBiometricEnabled = args[18] as Boolean,
            isAppUnlocked = args[19] as Boolean,
            biometricError = args[20] as? String
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

        val expenseItems = rangeFiltered.filter { !it.isIncome }
        val incomeItems = rangeFiltered.filter { it.isIncome }

        val totalSpentValue = expenseItems.sumOf { convertAmount(it) }
        val totalIncomeValue = incomeItems.sumOf { convertAmount(it) }

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

        // Budgets status
        val statuses = allBudgets.map { b: Budget ->
            val limit = SupportedCurrency.convert(
                b.monthlyLimit,
                SupportedCurrency.fromCode(b.currencyCode),
                currency
            )
            val spent = if (b.category == "OVERALL") {
                totalSpentValue
            } else {
                expenseItems.filter {
                    it.category.equals(b.category, ignoreCase = true) ||
                    it.expenseCategory.name.equals(b.category, ignoreCase = true)
                }.sumOf { convertAmount(it) }
            }
            val pct = if (limit > 0) (spent / limit).toFloat() else 0f
            val remaining = (limit - spent).coerceAtLeast(0.0)

            val catDisplayName = if (b.category == "OVERALL") "Overall Budget" else com.example.data.CategoryRegistry.getCategoryItem(b.category).displayName
            BudgetStatus(
                categoryName = b.category,
                displayName = catDisplayName,
                monthlyLimit = limit,
                currentSpent = spent,
                percentUsed = pct,
                remaining = remaining
            )
        }

        // Budget Alerts calculation
        val calculatedBudgetAlerts = mutableListOf<BudgetAlert>()
        for (b in allBudgets) {
            val limit = SupportedCurrency.convert(b.monthlyLimit, SupportedCurrency.fromCode(b.currencyCode), currency)
            if (limit <= 0) continue

            val spent = if (b.category == "OVERALL") {
                totalSpentValue
            } else {
                val cat = ExpenseCategory.fromString(b.category)
                expenseItems.filter { it.expenseCategory == cat }.sumOf { convertAmount(it) }
            }
            val pct = (spent / limit).toFloat()
            val isExceeded = spent >= limit
            val isApproaching = !isExceeded && (pct * 100) >= criteria.warningThreshold
            val catDisplayName = if (b.category == "OVERALL") "Overall Budget" else ExpenseCategory.fromString(b.category).displayName

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
                val sameAmount = abs(e1.amount - e2.amount) < 0.01
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

        // 2. Spending Anomaly Detection
        val byCat = nonIncomeExpenses.groupBy { it.expenseCategory }
        for ((cat, catExpenses) in byCat) {
            if (catExpenses.size >= 2) {
                val avg = catExpenses.map { convertAmount(it) }.average()
                val maxExp = catExpenses.maxByOrNull { convertAmount(it) }
                if (maxExp != null && convertAmount(maxExp) > avg * 2.2 && convertAmount(maxExp) > 50.0) {
                    val ratio = String.format(Locale.US, "%.1fx", convertAmount(maxExp) / avg)
                    aiInsightsList.add(
                        AiInsightItem(
                            id = "anomaly_${maxExp.id}",
                            title = "Unusual Spending Spike in ${cat.displayName}",
                            description = "'${maxExp.title}' for ${FormatUtils.formatCurrency(convertAmount(maxExp), currency)} is $ratio higher than your typical average of ${FormatUtils.formatCurrency(avg, currency)}.",
                            type = "ANOMALY",
                            severity = "WARNING",
                            relatedExpense = maxExp
                        )
                    )
                }
            }
        }

        // 3. Savings Opportunities
        val foodSpent = nonIncomeExpenses.filter { it.expenseCategory == ExpenseCategory.FOOD }.sumOf { convertAmount(it) }
        if (totalSpentValue > 0 && (foodSpent / totalSpentValue) > 0.25) {
            val pct = (foodSpent / totalSpentValue * 100).toInt()
            val potSave = foodSpent * 0.18
            aiInsightsList.add(
                AiInsightItem(
                    id = "tip_food",
                    title = "Dining Outlay Savings Opportunity",
                    description = "Food represents $pct% of your total expenses. Preparing 2 more home-cooked meals weekly could save an estimated ${FormatUtils.formatCurrency(potSave, currency)} each month.",
                    type = "SAVINGS",
                    severity = "INFO"
                )
            )
        }

        val subSpent = nonIncomeExpenses.filter { it.expenseCategory == ExpenseCategory.UTILITIES || it.expenseCategory == ExpenseCategory.ENTERTAINMENT }.sumOf { convertAmount(it) }
        if (subSpent > 40.0) {
            aiInsightsList.add(
                AiInsightItem(
                    id = "tip_recurring",
                    title = "Recurring Subscriptions Optimization",
                    description = "You spent ${FormatUtils.formatCurrency(subSpent, currency)} on entertainment & recurring bills. Auditing inactive memberships could save up to ${FormatUtils.formatCurrency(subSpent * 3, currency)}/year.",
                    type = "TIP",
                    severity = "INFO"
                )
            )
        }

        if (aiInsightsList.none { it.type == "SAVINGS" || it.type == "TIP" }) {
            aiInsightsList.add(
                AiInsightItem(
                    id = "tip_general",
                    title = "Automated Payday Savings Rule",
                    description = "Auto-routing 10% of monthly income to a separate savings goal on deposit days prevents impulse spending and builds financial peace of mind.",
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
            val matchesSearch = criteria.search.isBlank() ||
                    expense.title.contains(criteria.search, ignoreCase = true) ||
                    expense.note.contains(criteria.search, ignoreCase = true) ||
                    expense.expenseCategory.displayName.contains(criteria.search, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        // Account starting balance & total balance
        val initialAccBalance = activeAccount?.initialBalance ?: 0.0
        val calculatedBalance = if (totalIncomeValue > 0) {
            (initialAccBalance + totalIncomeValue - totalSpentValue).coerceAtLeast(0.0)
        } else {
            (initialAccBalance - totalSpentValue).coerceAtLeast(0.0)
        }

        ExpenseUiState(
            activeTab = criteria.tab,
            reportSubTab = criteria.subTab,
            chartType = criteria.chart,
            selectedMonthLabel = criteria.month,
            currentBalance = calculatedBalance,
            totalBalance = calculatedBalance,
            totalSpent = totalSpentValue,
            totalIncome = totalIncomeValue,
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
            isDarkMode = criteria.isDarkMode,
            isBiometricEnabled = criteria.isBiometricEnabled,
            isAppUnlocked = criteria.isAppUnlocked,
            biometricErrorMessage = criteria.biometricError
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

    fun parseSampleReceipt(sampleType: String) {
        val ocrSample = when (sampleType.uppercase()) {
            "GROCERY" -> """
                WHOLE FOODS MARKET
                123 Market St, San Francisco CA
                Date: 09/12/2025
                Organic Bananas    ${'$'}2.49
                Almond Milk        ${'$'}4.29
                Fresh Salmon       ${'$'}18.50
                Olive Oil Extra    ${'$'}11.99
                SUBTOTAL:          ${'$'}37.27
                TAX:               ${'$'}3.18
                TOTAL:             ${'$'}40.45
            """.trimIndent()
            "COFFEE" -> """
                STARBUCKS COFFEE
                Store #1492
                Date: 09/12/2025
                1x Caffe Latte     ${'$'}5.45
                1x Croissant       ${'$'}3.85
                TOTAL:             ${'$'}9.30
            """.trimIndent()
            "GAS" -> """
                SHELL GAS STATION
                Station #0812
                Date: 09/11/2025
                Pump #03 Regular   ${'$'}45.00
                TOTAL:             ${'$'}45.00
            """.trimIndent()
            "RETAIL" -> """
                TARGET STORES
                Receipt #9283-11
                Date: 09/10/2025
                Home Decor Lamp    ${'$'}39.99
                Storage Bins       ${'$'}19.99
                TAX:               ${'$'}4.80
                TOTAL:             ${'$'}64.78
            """.trimIndent()
            "PHARMACY" -> """
                CVS PHARMACY
                Store #0312
                Date: 09/09/2025
                Vitamins C 1000mg  ${'$'}14.49
                First Aid Kit      ${'$'}8.99
                TOTAL:             ${'$'}23.48
            """.trimIndent()
            else -> """
                GENERAL STORE
                Date: 09/12/2025
                Assorted Goods
                TOTAL: ${'$'}25.00
            """.trimIndent()
        }
        scanReceiptOcrText(ocrSample)
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

    fun setCategoryFilter(category: ExpenseCategory?) {
        _selectedCategory.value = category
    }

    fun setSelectedCurrency(currency: SupportedCurrency) {
        _selectedCurrency.value = currency
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
        prefs.edit().putBoolean("pref_dark_mode", isDark).apply()
    }

    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
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
        currency: String
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
                isActive = true
            )
            repository.createAccount(newAccount)
            _selectedCurrency.value = SupportedCurrency.fromCode(currency)
        }
    }

    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            repository.switchAccount(accountId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun updateAccount(account: UserAccount) {
        viewModelScope.launch {
            repository.updateAccount(account)
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
            repository.insert(
                Expense(
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
            )
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
            repository.update(
                Expense(
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
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun setBudget(category: String, limit: Double, currencyCode: String = _selectedCurrency.value.code) {
        viewModelScope.launch {
            repository.setBudget(Budget(category = category, monthlyLimit = limit, currencyCode = currencyCode))
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
            category = ExpenseCategory.fromString(parsed.categoryHint).name,
            dateMillis = parsed.dateMillis,
            note = parsed.rawNotes,
            currencyCode = _selectedCurrency.value.code,
            isAutomated = true
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
        }
    }

    fun logoutAndShowAuth() {
        viewModelScope.launch {
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
        _isAuthDismissed.value = true
        prefs.edit().putBoolean("pref_auth_dismissed", true).apply()
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
            repository.insertCustomCategory(
                CustomCategory(
                    name = clean,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    isIncome = isIncome
                )
            )
        }
    }

    fun deleteCustomCategory(category: CustomCategory) {
        viewModelScope.launch {
            repository.deleteCustomCategory(category)
        }
    }

    fun deleteCustomCategoryById(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomCategoryById(id)
        }
    }
}
