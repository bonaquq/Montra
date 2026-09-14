package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreService(private val context: Context) {
    private val TAG = "FirestoreService"

    private val db: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            val firestore = FirebaseFirestore.getInstance()
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not initialized: ${e.message}")
            null
        }
    }

    private var expensesListener: ListenerRegistration? = null
    private var budgetsListener: ListenerRegistration? = null

    // -------------------------------------------------------------
    // USER ACCOUNT PERSISTENCE
    // -------------------------------------------------------------
    suspend fun saveUserAccount(userId: String, account: UserAccount) {
        val firestore = db ?: return
        try {
            val data = hashMapOf(
                "id" to account.id,
                "name" to account.name,
                "email" to account.email,
                "currencyCode" to account.currencyCode,
                "initialBalance" to account.initialBalance,
                "profilePictureUri" to (account.profilePictureUri ?: ""),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(userId)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "User account synced to Firestore for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user account to Firestore", e)
        }
    }

    // -------------------------------------------------------------
    // EXPENSES / TRANSACTIONS PERSISTENCE
    // -------------------------------------------------------------
    suspend fun saveExpense(userId: String, expense: Expense) {
        val firestore = db ?: return
        try {
            val data = hashMapOf(
                "id" to expense.id,
                "title" to expense.title,
                "amount" to expense.amount,
                "category" to expense.category,
                "dateMillis" to expense.dateMillis,
                "note" to expense.note,
                "currencyCode" to expense.currencyCode,
                "receiptUri" to (expense.receiptUri ?: ""),
                "isAutomated" to expense.isAutomated,
                "transactionType" to expense.transactionType,
                "userId" to userId,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(userId)
                .collection("expenses")
                .document(expense.id.toString())
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Expense ${expense.id} synced to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save expense to Firestore", e)
        }
    }

    suspend fun deleteExpense(userId: String, expenseId: Long) {
        val firestore = db ?: return
        try {
            firestore.collection("users")
                .document(userId)
                .collection("expenses")
                .document(expenseId.toString())
                .delete()
                .await()
            Log.d(TAG, "Expense $expenseId deleted from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete expense from Firestore", e)
        }
    }

    fun listenToExpenses(userId: String, onUpdate: (List<Expense>) -> Unit) {
        val firestore = db ?: return
        expensesListener?.remove()
        expensesListener = firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen to expenses failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                            val title = doc.getString("title") ?: doc.getString("description") ?: "Expense"
                            val amount = doc.getDouble("amount") ?: 0.0
                            val category = doc.getString("category") ?: "OTHER"
                            val dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis()
                            val note = doc.getString("note") ?: ""
                            val currencyCode = doc.getString("currencyCode") ?: "USD"
                            val receiptUri = doc.getString("receiptUri")?.takeIf { it.isNotEmpty() }
                                ?: doc.getString("receiptUriString")?.takeIf { it.isNotEmpty() }
                            val isAutomated = doc.getBoolean("isAutomated") ?: false
                            val transactionType = doc.getString("transactionType")
                                ?: if (doc.getBoolean("isIncome") == true) "INCOME" else "EXPENSE"
                            Expense(
                                id = id,
                                title = title,
                                amount = amount,
                                category = category,
                                dateMillis = dateMillis,
                                note = note,
                                currencyCode = currencyCode,
                                receiptUri = receiptUri,
                                isAutomated = isAutomated,
                                transactionType = transactionType
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    onUpdate(list)
                }
            }
    }

    // -------------------------------------------------------------
    // BUDGETS PERSISTENCE
    // -------------------------------------------------------------
    suspend fun saveBudget(userId: String, budget: Budget) {
        val firestore = db ?: return
        try {
            val data = hashMapOf(
                "category" to budget.category,
                "monthlyLimit" to budget.monthlyLimit,
                "currencyCode" to budget.currencyCode,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(userId)
                .collection("budgets")
                .document(budget.category)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save budget to Firestore", e)
        }
    }

    suspend fun deleteBudget(userId: String, categoryKey: String) {
        val firestore = db ?: return
        try {
            firestore.collection("users")
                .document(userId)
                .collection("budgets")
                .document(categoryKey)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete budget from Firestore", e)
        }
    }

    fun listenToBudgets(userId: String, onUpdate: (List<Budget>) -> Unit) {
        val firestore = db ?: return
        budgetsListener?.remove()
        budgetsListener = firestore.collection("users")
            .document(userId)
            .collection("budgets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen to budgets failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val category = doc.getString("category") ?: doc.id
                            val monthlyLimit = doc.getDouble("monthlyLimit") ?: 0.0
                            val currencyCode = doc.getString("currencyCode") ?: "USD"
                            Budget(
                                category = category,
                                monthlyLimit = monthlyLimit,
                                currencyCode = currencyCode
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    onUpdate(list)
                }
            }
    }

    // -------------------------------------------------------------
    // CUSTOM CATEGORIES PERSISTENCE
    // -------------------------------------------------------------
    suspend fun saveCustomCategory(userId: String, category: CustomCategory) {
        val firestore = db ?: return
        try {
            val data = hashMapOf(
                "id" to category.id,
                "name" to category.name,
                "iconKey" to category.iconKey,
                "colorHex" to category.colorHex,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(userId)
                .collection("custom_categories")
                .document(category.name)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save custom category to Firestore", e)
        }
    }

    suspend fun deleteCustomCategory(userId: String, categoryName: String) {
        val firestore = db ?: return
        try {
            firestore.collection("users")
                .document(userId)
                .collection("custom_categories")
                .document(categoryName)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete custom category from Firestore", e)
        }
    }

    // -------------------------------------------------------------
    // FULL INITIAL SYNC TO CLOUD
    // -------------------------------------------------------------
    suspend fun syncAllLocalDataToFirestore(
        userId: String,
        account: UserAccount?,
        expenses: List<Expense>,
        budgets: List<Budget>,
        customCategories: List<CustomCategory>
    ) {
        if (db == null) return
        try {
            if (account != null) {
                saveUserAccount(userId, account)
            }
            expenses.forEach { saveExpense(userId, it) }
            budgets.forEach { saveBudget(userId, it) }
            customCategories.forEach { saveCustomCategory(userId, it) }
            Log.d(TAG, "Completed full sync of ${expenses.size} expenses to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncAllLocalDataToFirestore", e)
        }
    }

    fun stopListeners() {
        expensesListener?.remove()
        expensesListener = null
        budgetsListener?.remove()
        budgetsListener = null
    }
}
