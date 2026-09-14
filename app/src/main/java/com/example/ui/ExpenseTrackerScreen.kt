package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Expense
import com.example.data.ExpenseCategory
import com.example.ui.components.AccountManageModal
import com.example.ui.components.AddExpenseScreen
import com.example.ui.components.AnalyticsScreenContent
import com.example.ui.components.AuthScreen
import com.example.ui.components.BiometricLockScreen
import com.example.ui.components.CategoriesScreenContent
import com.example.ui.components.CreateAccountModal
import com.example.ui.components.CreateCategoryModal
import com.example.ui.components.DeveloperConsoleModal
import com.example.ui.components.DeveloperPasscodeDialog
import com.example.ui.components.ExpenseBottomNav
import com.example.ui.components.HomeScreenContent
import com.example.ui.components.ManageBudgetsSheet
import com.example.ui.components.SettingsScreenContent
import com.example.ui.components.TransactionDetailModal
import com.example.ui.components.TransactionsScreenContent
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraTextPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isAddExpenseScreenOpen by remember { mutableStateOf(false) }
    var isCreateAccountModalOpen by remember { mutableStateOf(false) }
    var isAccountManageModalOpen by remember { mutableStateOf(false) }
    var isCreateCategoryModalOpen by remember { mutableStateOf(false) }
    var isManageBudgetsSheetOpen by remember { mutableStateOf(false) }
    var isDeveloperConsoleOpen by remember { mutableStateOf(false) }
    var isDeveloperPasscodeDialogOpen by remember { mutableStateOf(false) }
    var selectedExpenseForDetail by remember { mutableStateOf<Expense?>(null) }
    var isGoogleLoginModalOpen by remember { mutableStateOf(false) }

    val openDeveloperFlow = {
        if (uiState.isDeveloperUnlocked) {
            isDeveloperConsoleOpen = true
        } else {
            isDeveloperPasscodeDialogOpen = true
        }
    }

    if (uiState.isBiometricEnabled && !uiState.isAppUnlocked) {
        BiometricLockScreen(
            uiState = uiState,
            onUnlockSuccess = {
                viewModel.unlockApp()
            },
            onVerifyPin = { pin, onResult ->
                scope.launch {
                    val valid = viewModel.verifyPinOrPassword(pin)
                    onResult(valid)
                }
            }
        )
    } else if (uiState.activeAccount == null && uiState.authUser == null || (!uiState.isAuthDismissed && uiState.authUser == null)) {
        AuthScreen(
            onSignIn = { email, pass ->
                viewModel.signInWithFirebase(email, pass)
            },
            onSignUp = { email, pass, name, initialBalance, currency ->
                viewModel.signUpWithFirebase(email, pass, name, initialBalance, currency)
            },
            onContinueAsGuest = {
                viewModel.continueAsGuest()
            },
            onSignInWithGoogle = {
                viewModel.signInWithGoogle(
                    context = context,
                    onRequireGoogleLoginPrompt = {
                        isGoogleLoginModalOpen = true
                    }
                )
            },
            onSignInWithCustomGoogle = { email, name ->
                viewModel.signInWithCustomGoogle(email, name)
            },
            showGoogleLoginDialogExternally = isGoogleLoginModalOpen,
            onDismissGoogleLoginDialog = {
                isGoogleLoginModalOpen = false
            },
            isLoading = uiState.isAuthLoading,
            errorMessage = uiState.authErrorMessage
        )
    } else if (isAddExpenseScreenOpen) {
        AddExpenseScreen(
            onBack = {
                isAddExpenseScreenOpen = false
                viewModel.clearScannedReceipt()
            },
            onAddExpense = { amount, category, dateMillis, description, isIncome, currencyCode ->
                viewModel.addExpense(
                    title = description.ifEmpty { category },
                    amount = amount,
                    category = category,
                    dateMillis = dateMillis,
                    note = description,
                    currencyCode = currencyCode,
                    isIncome = isIncome
                )
                isAddExpenseScreenOpen = false
                viewModel.clearScannedReceipt()
            },
            onSaveBudget = { category, limit ->
                viewModel.setBudget(category, limit)
            },
            budgetStatuses = uiState.budgetStatuses,
            onScanReceipt = { uri ->
                viewModel.scanReceiptImage(context, uri)
            },
            isScanningReceipt = uiState.isScanningReceipt,
            scannedReceiptResult = uiState.scannedReceiptResult,
            onClearScannedReceipt = {
                viewModel.clearScannedReceipt()
            },
            selectedCurrency = uiState.selectedCurrency,
            isDeveloperMode = uiState.isDeveloperUnlocked
        )
    } else {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(MontraBackground),
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (uiState.activeTab != AppTab.SETTINGS) {
                    ExpenseBottomNav(
                        activeTab = uiState.activeTab,
                        onTabSelected = { tab ->
                            viewModel.setActiveTab(tab)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MontraTextPrimary)
                    }
                } else {
                    AnimatedContent(
                        targetState = uiState.activeTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            AppTab.HOME -> {
                                HomeScreenContent(
                                    uiState = uiState,
                                    onOpenSettings = {
                                        viewModel.setActiveTab(AppTab.SETTINGS)
                                    },
                                    onOpenAddExpense = {
                                        isAddExpenseScreenOpen = true
                                    },
                                    onSeeAllTransactions = {
                                        viewModel.setTransactionFilter("ALL")
                                        viewModel.setActiveTab(AppTab.TRANSACTIONS)
                                    },
                                    onSeeAllTransactionsWithFilter = { filter ->
                                        viewModel.setTransactionFilter(filter)
                                        viewModel.setActiveTab(AppTab.TRANSACTIONS)
                                    },
                                    onManageBudgets = {
                                        isManageBudgetsSheetOpen = true
                                    },
                                    onToggleBudgetPeriod = { period ->
                                        viewModel.setOverallBudgetPeriod(period)
                                    },
                                    onToggleBalanceVisibility = {
                                        viewModel.toggleBalanceVisibility()
                                    },
                                    onExpenseClick = { exp ->
                                        selectedExpenseForDetail = exp
                                    }
                                )
                            }
                            AppTab.TRANSACTIONS -> {
                                TransactionsScreenContent(
                                    uiState = uiState,
                                    onFilterChanged = { filter ->
                                        viewModel.setTransactionFilter(filter)
                                    },
                                    onSearchChanged = { query ->
                                        viewModel.setSearchQuery(query)
                                    },
                                    onExpenseClick = { exp ->
                                        selectedExpenseForDetail = exp
                                    }
                                )
                            }
                            AppTab.ANALYTICS -> {
                                AnalyticsScreenContent(
                                    uiState = uiState,
                                    onPeriodChanged = { period ->
                                        viewModel.setAnalyticsPeriod(period)
                                    },
                                    onNavigateDate = { delta ->
                                        viewModel.navigateAnalyticsDate(delta)
                                    },
                                    onSetCustomDateRange = { start, end ->
                                        viewModel.setAnalyticsCustomDateRange(start, end)
                                    },
                                    onDismissBudgetAlert = { cat ->
                                        viewModel.dismissBudgetAlert(cat)
                                    },
                                    onSetBudgetThreshold = { threshold ->
                                        viewModel.setBudgetWarningThreshold(threshold)
                                    }
                                )
                            }
                            AppTab.CATEGORIES -> {
                                CategoriesScreenContent(
                                    uiState = uiState,
                                    onAddCategory = {
                                        isCreateCategoryModalOpen = true
                                    },
                                    onCategoryClick = { catName ->
                                        val stdCat = ExpenseCategory.fromString(catName)
                                        viewModel.setCategoryFilter(stdCat)
                                        viewModel.setActiveTab(AppTab.TRANSACTIONS)
                                    },
                                    onManageBudgets = {
                                        isManageBudgetsSheetOpen = true
                                    },
                                    onDeleteCustomCategory = { customCat ->
                                        viewModel.deleteCustomCategory(customCat)
                                    }
                                )
                            }
                            AppTab.SETTINGS -> {
                                SettingsScreenContent(
                                    uiState = uiState,
                                    onBack = {
                                        viewModel.setActiveTab(AppTab.HOME)
                                    },
                                    onOpenAccountManage = {
                                        isAccountManageModalOpen = true
                                    },
                                    onOpenCreateAccount = {
                                        viewModel.showAuth()
                                    },
                                    onLogout = {
                                        viewModel.firebaseSignOut()
                                    },
                                    onCurrencySelected = { cur ->
                                        viewModel.setSelectedCurrency(cur)
                                    },
                                    onSetTheme = { isDark ->
                                        viewModel.setDarkMode(isDark)
                                    },
                                    onToggleTheme = {
                                        viewModel.toggleDarkMode()
                                    },
                                    onToggleBiometric = { enable ->
                                        viewModel.setBiometricEnabled(enable)
                                    },
                                    onLockAppNow = {
                                        viewModel.lockApp()
                                    },
                                    onClearAllTransactions = {
                                        viewModel.deleteAllTransactions()
                                    }
                                )
                            }
                            else -> {
                                HomeScreenContent(
                                    uiState = uiState,
                                    onOpenSettings = {
                                        viewModel.setActiveTab(AppTab.SETTINGS)
                                    },
                                    onOpenAddExpense = {
                                        isAddExpenseScreenOpen = true
                                    },
                                    onSeeAllTransactions = {
                                        viewModel.setActiveTab(AppTab.TRANSACTIONS)
                                    },
                                    onToggleBalanceVisibility = {
                                        viewModel.toggleBalanceVisibility()
                                    },
                                    onExpenseClick = { exp ->
                                        selectedExpenseForDetail = exp
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Account Modal
    if (isCreateAccountModalOpen) {
        CreateAccountModal(
            onDismiss = { isCreateAccountModalOpen = false },
            onCreateAccount = { name, email, pin, balance, currency, profilePictureUri ->
                viewModel.createAccount(
                    name = name,
                    email = email,
                    pin = pin,
                    initialBalance = balance,
                    currency = currency,
                    profilePictureUri = profilePictureUri
                )
                isCreateAccountModalOpen = false
            }
        )
    }

    // Account Management Modal
    if (isAccountManageModalOpen) {
        AccountManageModal(
            activeAccount = uiState.activeAccount,
            allAccounts = uiState.allAccounts,
            onDismiss = { isAccountManageModalOpen = false },
            onOpenCreateAccount = {
                isAccountManageModalOpen = false
                isCreateAccountModalOpen = true
            },
            onSwitchAccount = { accountId ->
                viewModel.switchAccount(accountId)
                isAccountManageModalOpen = false
            },
            onLogout = {
                viewModel.logoutAndShowAuth()
                isAccountManageModalOpen = false
            },
            onUpdateProfilePicture = { uriString ->
                viewModel.updateProfilePicture(uriString)
            },
            onUpdateAccountProfile = { name, email, pfp ->
                viewModel.updateAccountProfile(name, email, pfp)
            },
            onOpenDeveloperOptions = {
                isAccountManageModalOpen = false
                openDeveloperFlow()
            }
        )
    }

    // Transaction Detail & Edit/Delete Modal
    selectedExpenseForDetail?.let { expense ->
        TransactionDetailModal(
            expense = expense,
            currency = uiState.selectedCurrency,
            onDismiss = { selectedExpenseForDetail = null },
            onUpdateExpense = { id, title, amount, category, dateMillis, note, currencyCode, receiptUri, isIncome ->
                viewModel.updateExpense(
                    id = id,
                    title = title,
                    amount = amount,
                    category = category,
                    dateMillis = dateMillis,
                    note = note,
                    currencyCode = currencyCode,
                    receiptUri = receiptUri,
                    isIncome = isIncome
                )
                selectedExpenseForDetail = null
            },
            onDeleteExpense = { exp ->
                viewModel.deleteExpense(exp)
                selectedExpenseForDetail = null
            }
        )
    }

    // Create Category Modal
    if (isCreateCategoryModalOpen) {
        CreateCategoryModal(
            onDismiss = { isCreateCategoryModalOpen = false },
            onCreateCategory = { name, iconKey, colorHex ->
                viewModel.createCustomCategory(name, iconKey, colorHex)
            }
        )
    }

    // Manage Budgets Modal Sheet
    if (isManageBudgetsSheetOpen) {
        ManageBudgetsSheet(
            budgetStatuses = uiState.budgetStatuses,
            currency = uiState.selectedCurrency,
            onSaveBudget = { category, limit ->
                viewModel.setBudget(category, limit)
            },
            onDeleteBudget = { category ->
                viewModel.deleteBudget(category)
            },
            onDismiss = { isManageBudgetsSheetOpen = false }
        )
    }

    // Developer Passcode Authentication Dialog
    if (isDeveloperPasscodeDialogOpen) {
        DeveloperPasscodeDialog(
            onDismiss = { isDeveloperPasscodeDialogOpen = false },
            onCodeVerified = {
                viewModel.setDeveloperUnlocked(true)
                isDeveloperPasscodeDialogOpen = false
                isDeveloperConsoleOpen = true
            }
        )
    }

    // Developer Console & Diagnostic Modal
    if (isDeveloperConsoleOpen) {
        DeveloperConsoleModal(
            uiState = uiState,
            onDismiss = { isDeveloperConsoleOpen = false },
            onLockDeveloperMode = {
                viewModel.setDeveloperUnlocked(false)
                isDeveloperConsoleOpen = false
            },
            onInjectSampleData = {
                viewModel.injectDeveloperSampleData()
            },
            onInjectSingleTransaction = { title, amount, category, isIncome ->
                viewModel.injectSingleMockTransaction(title, amount, category, isIncome)
            },
            onSimulateReceipt = {
                viewModel.simulateReceiptScan()
                isDeveloperConsoleOpen = false
                isAddExpenseScreenOpen = true
            },
            onScanReceiptUri = { uri ->
                viewModel.scanReceiptImage(context, uri)
                isDeveloperConsoleOpen = false
                isAddExpenseScreenOpen = true
            },
            onOpenAddExpenseWithScan = {
                isDeveloperConsoleOpen = false
                isAddExpenseScreenOpen = true
            },
            onClearAllTransactions = {
                viewModel.deleteAllTransactions()
            }
        )
    }
}
