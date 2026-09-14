package com.example.util

import com.example.data.SupportedCurrency
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDisplayDate(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return SimpleDateFormat("MMM d", Locale.US).format(Date(timestamp))
    }
}

object FormatUtils {
    fun formatCurrency(amount: Double, currency: SupportedCurrency = SupportedCurrency.MVR): String {
        return when (currency) {
            SupportedCurrency.USD -> String.format(Locale.US, "$%.2f", amount)
            SupportedCurrency.EUR -> String.format(Locale.GERMANY, "€%.2f", amount)
            SupportedCurrency.GBP -> String.format(Locale.UK, "£%.2f", amount)
            SupportedCurrency.JPY -> String.format(Locale.JAPAN, "¥%,.0f", amount)
            SupportedCurrency.INR -> String.format(Locale.forLanguageTag("en-IN"), "₹%,.2f", amount)
            SupportedCurrency.CAD -> String.format(Locale.CANADA, "CA$%.2f", amount)
            SupportedCurrency.AUD -> String.format(Locale.forLanguageTag("en-AU"), "A$%.2f", amount)
            SupportedCurrency.MVR -> String.format(Locale.US, "Rf %,.2f", amount)
        }
    }

    fun formatDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1

        val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

        return when {
            isToday -> "Today, $timeString"
            isYesterday -> "Yesterday, $timeString"
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) -> {
                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
            }
            else -> {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    fun formatShortDate(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
