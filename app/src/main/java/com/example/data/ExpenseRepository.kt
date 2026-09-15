package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val userAccountDao: UserAccountDao,
    private val customCategoryDao: CustomCategoryDao
) {

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
    val allAccounts: Flow<List<UserAccount>> = userAccountDao.getAllAccounts()
    val activeAccount: Flow<UserAccount?> = userAccountDao.getActiveAccount()
    val allCustomCategories: Flow<List<CustomCategory>> = customCategoryDao.getAllCategories()

    fun getExpensesInRange(startMillis: Long, endMillis: Long): Flow<List<Expense>> =
        expenseDao.getExpensesInRange(startMillis, endMillis)

    suspend fun insert(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun insertExpenses(expenses: List<Expense>) = expenseDao.insertExpenses(expenses)

    suspend fun getAllExpensesOnce(): List<Expense> = expenseDao.getAllExpensesOnce()

    suspend fun update(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun delete(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun deleteById(id: Long) = expenseDao.deleteExpenseById(id)

    suspend fun setBudget(budget: Budget) = budgetDao.insertBudget(budget)

    suspend fun insertBudgets(budgets: List<Budget>) = budgetDao.insertBudgets(budgets)

    suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)

    suspend fun getAllBudgetsOnce(): List<Budget> = budgetDao.getAllBudgetsOnce()

    suspend fun deleteBudgetByCategory(category: String) {
        val existing = budgetDao.getBudgetForCategory(category)
        if (existing != null) {
            budgetDao.deleteBudget(existing)
        }
    }

    suspend fun insertCustomCategory(category: CustomCategory): Long =
        customCategoryDao.insertCategory(category)

    suspend fun insertCustomCategories(categories: List<CustomCategory>) =
        customCategoryDao.insertCategories(categories)

    suspend fun deleteCustomCategory(category: CustomCategory) =
        customCategoryDao.deleteCategory(category)

    suspend fun deleteCustomCategoryById(id: Long) =
        customCategoryDao.deleteCategoryById(id)

    suspend fun getAllCustomCategoriesOnce(): List<CustomCategory> =
        customCategoryDao.getAllCategoriesOnce()

    suspend fun createAccount(account: UserAccount) {
        userAccountDao.deactivateAll()
        userAccountDao.insertAccount(account.copy(isActive = true))
    }

    suspend fun switchAccount(accountId: String) {
        userAccountDao.deactivateAll()
        userAccountDao.activateAccount(accountId)
    }

    suspend fun updateAccount(account: UserAccount) {
        userAccountDao.updateAccount(account)
    }

    suspend fun findByEmail(email: String): UserAccount? =
        userAccountDao.findByEmail(email.trim().lowercase())

    suspend fun getActiveAccountOnce(): UserAccount? =
        userAccountDao.getActiveAccountOnce()

    suspend fun logout() {
        userAccountDao.deactivateAll()
    }

    suspend fun deleteDemoExpenses(): Int = expenseDao.deleteDemoExpenses()

    suspend fun deleteAllExpenses(): Int = expenseDao.deleteAllExpenses()

    suspend fun resetDemoAccountAndBalances() {
        userAccountDao.resetDemoBalances()
        userAccountDao.resetDemoAccount()
    }

    suspend fun resetAllAccountBalances() {
        userAccountDao.resetAllBalances()
    }

    suspend fun resetAllCategoryBudgetsToZero() = budgetDao.resetAllBudgetsToZero()

    suspend fun seedInitialDataIfEmpty() {
        // Reset any legacy demo balances or demo accounts from earlier releases
        resetDemoAccountAndBalances()

        if (userAccountDao.getAccountCount() == 0) {
            val defaultAccount = UserAccount(
                id = "acc_primary",
                name = "Personal Account",
                email = "user@montra.app",
                pin = "1234",
                initialBalance = 0.0,
                currencyCode = "MVR",
                isActive = true
            )
            userAccountDao.insertAccount(defaultAccount)
        }

        // Clean up any legacy demo expenses from previous app versions
        expenseDao.deleteDemoExpenses()

        // Reset any previous non-zero demo/template category budget values to 0.0
        budgetDao.resetLegacyDefaultBudgets()

        if (budgetDao.getAllBudgetsOnce().isEmpty()) {
            val initialBudgets = listOf(
                Budget(category = "OVERALL", monthlyLimit = 0.0, currencyCode = "MVR"),
                Budget(category = ExpenseCategory.RENT.name, monthlyLimit = 0.0, currencyCode = "MVR"),
                Budget(category = ExpenseCategory.FOOD.name, monthlyLimit = 0.0, currencyCode = "MVR"),
                Budget(category = ExpenseCategory.UTILITIES.name, monthlyLimit = 0.0, currencyCode = "MVR"),
                Budget(category = ExpenseCategory.TRANSPORT.name, monthlyLimit = 0.0, currencyCode = "MVR"),
                Budget(category = ExpenseCategory.SHOPPING.name, monthlyLimit = 0.0, currencyCode = "MVR")
            )
            budgetDao.insertBudgets(initialBudgets)
        }

        if (customCategoryDao.getCategoryCount() == 0) {
            val initialCustom = listOf(
                CustomCategory(name = "Education", iconKey = "School", colorHex = "#2563EB"),
                CustomCategory(name = "Travel", iconKey = "Flight", colorHex = "#0D9488"),
                CustomCategory(name = "Coffee", iconKey = "LocalCafe", colorHex = "#EA580C"),
                CustomCategory(name = "Pets", iconKey = "Pets", colorHex = "#10B981"),
                CustomCategory(name = "Fitness", iconKey = "FitnessCenter", colorHex = "#8B5CF6")
            )
            customCategoryDao.insertCategories(initialCustom)
        }
    }
}
