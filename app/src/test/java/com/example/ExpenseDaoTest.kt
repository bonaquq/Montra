package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.Budget
import com.example.data.BudgetDao
import com.example.data.Expense
import com.example.data.ExpenseCategory
import com.example.data.ExpenseDao
import com.example.data.ExpenseDatabase
import com.example.data.SupportedCurrency
import com.example.util.ReceiptParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExpenseDaoTest {

    private lateinit var db: ExpenseDatabase
    private lateinit var dao: ExpenseDao
    private lateinit var budgetDao: BudgetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExpenseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.expenseDao()
        budgetDao = db.budgetDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveExpense() = runBlocking {
        val expense = Expense(
            title = "Espresso",
            amount = 4.50,
            category = ExpenseCategory.FOOD.name,
            dateMillis = 1000L,
            note = "Morning coffee",
            currencyCode = "EUR",
            isAutomated = true
        )
        val id = dao.insertExpense(expense)
        val retrieved = dao.getExpenseById(id)

        assertNotNull(retrieved)
        assertEquals("Espresso", retrieved?.title)
        assertEquals(4.50, retrieved?.amount ?: 0.0, 0.001)
        assertEquals(ExpenseCategory.FOOD, retrieved?.expenseCategory)
        assertEquals("EUR", retrieved?.currencyCode)
        assertTrue(retrieved?.isAutomated == true)
    }

    @Test
    fun deleteExpense() = runBlocking {
        val expense = Expense(
            title = "Movie Ticket",
            amount = 15.0,
            category = ExpenseCategory.ENTERTAINMENT.name,
            dateMillis = 2000L
        )
        val id = dao.insertExpense(expense)
        val listBefore = dao.getAllExpenses().first()
        assertEquals(1, listBefore.size)

        dao.deleteExpenseById(id)
        val listAfter = dao.getAllExpenses().first()
        assertEquals(0, listAfter.size)
    }

    @Test
    fun budgetInsertionAndRetrieval() = runBlocking {
        val budget = Budget(
            category = "FOOD",
            monthlyLimit = 350.0,
            currencyCode = "USD"
        )
        budgetDao.insertBudget(budget)

        val retrieved = budgetDao.getBudgetForCategory("FOOD")
        assertNotNull(retrieved)
        assertEquals(350.0, retrieved?.monthlyLimit ?: 0.0, 0.001)
    }

    @Test
    fun smartCategorizationPredictsCorrectly() {
        assertEquals(ExpenseCategory.FOOD, ExpenseCategory.predictCategory("Starbucks Caramel Macchiato"))
        assertEquals(ExpenseCategory.TRANSPORT, ExpenseCategory.predictCategory("Uber ride to airport"))
        assertEquals(ExpenseCategory.UTILITIES, ExpenseCategory.predictCategory("Electric power utility bill"))
        assertEquals(ExpenseCategory.ENTERTAINMENT, ExpenseCategory.predictCategory("Netflix monthly streaming"))
        assertEquals(ExpenseCategory.HEALTH, ExpenseCategory.predictCategory("CVS Pharmacy prescription"))
    }

    @Test
    fun automatedBankTextParserExtractsTransaction() {
        val alert = "Chase Alert: You spent $45.20 at Trader Joe's on 09/12"
        val parsed = ReceiptParser.parseAutomatedBankText(alert)

        assertEquals(45.20, parsed.amount, 0.01)
        assertEquals(ExpenseCategory.FOOD.name, parsed.categoryHint)
    }

    @Test
    fun multiCurrencyConversionWorks() {
        val amountInEur = 100.0
        val convertedUsd = SupportedCurrency.convert(amountInEur, SupportedCurrency.EUR, SupportedCurrency.USD)
        assertEquals(108.0, convertedUsd, 0.01)
    }
}
