package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val id: String = "user_default",
    val name: String = "Personal Account",
    val email: String = "user@montra.app",
    val pin: String = "1234",
    val initialBalance: Double = 0.0,
    val currencyCode: String = "USD",
    val isActive: Boolean = true,
    val profilePictureUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val initials: String
        get() {
            val parts = name.trim().split("\\s+".toRegex())
            return when {
                parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
                else -> "MO"
            }
        }
}
