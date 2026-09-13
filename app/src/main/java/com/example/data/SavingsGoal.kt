package com.example.data

data class SavingsGoal(
    val id: String,
    val title: String,
    val currentAmount: Double,
    val targetAmount: Double,
    val warningNote: String? = null,
    val categoryIcon: String = "HOUSE"
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1.5f) else 0f

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}
