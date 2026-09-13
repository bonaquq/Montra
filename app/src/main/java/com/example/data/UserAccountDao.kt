package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccount(): Flow<UserAccount?>

    @Query("SELECT * FROM user_accounts WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAccountOnce(): UserAccount?

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getAccountCount(): Int

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun findByEmail(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccount)

    @Update
    suspend fun updateAccount(account: UserAccount)

    @Query("UPDATE user_accounts SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE user_accounts SET isActive = 1 WHERE id = :id")
    suspend fun activateAccount(id: String)

    @Query("UPDATE user_accounts SET initialBalance = 0.0")
    suspend fun resetAllBalances()

    @Query("UPDATE user_accounts SET initialBalance = 0.0 WHERE initialBalance = 1248.32 OR id = 'acc_alex_morgan' OR email = 'alex.morgan@montra.app'")
    suspend fun resetDemoBalances()

    @Query("UPDATE user_accounts SET name = 'Personal Account', email = 'user@montra.app', initialBalance = 0.0 WHERE id = 'acc_alex_morgan' OR email = 'alex.morgan@montra.app'")
    suspend fun resetDemoAccount()

    @Query("DELETE FROM user_accounts WHERE id = :id")
    suspend fun deleteAccount(id: String)
}
