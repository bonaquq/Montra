package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsOnce(): List<Budget>

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun getBudgetCount(): Int

    @Query("SELECT * FROM budgets WHERE category = :category")
    suspend fun getBudgetForCategory(category: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<Budget>)

    @Query("UPDATE budgets SET monthlyLimit = 0.0 WHERE monthlyLimit IN (1500.0, 850.0, 200.0, 150.0, 60.0, 100.0)")
    suspend fun resetLegacyDefaultBudgets()

    @Query("UPDATE budgets SET monthlyLimit = 0.0")
    suspend fun resetAllBudgetsToZero()

    @Delete
    suspend fun deleteBudget(budget: Budget)
}
