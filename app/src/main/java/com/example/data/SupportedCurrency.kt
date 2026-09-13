package com.example.data

enum class SupportedCurrency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val exchangeRateToUSD: Double // 1 unit of this currency = exchangeRateToUSD in USD
) {
    USD("USD", "$", "US Dollar", 1.0),
    EUR("EUR", "€", "Euro", 1.08),
    GBP("GBP", "£", "British Pound", 1.28),
    JPY("JPY", "¥", "Japanese Yen", 0.0066),
    INR("INR", "₹", "Indian Rupee", 0.012),
    CAD("CAD", "CA$", "Canadian Dollar", 0.74),
    AUD("AUD", "A$", "Australian Dollar", 0.65),
    MVR("MVR", "Rf", "Maldivian Rufiyaa", 0.065);

    companion object {
        fun fromCode(code: String?): SupportedCurrency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
        }

        fun convert(amount: Double, from: SupportedCurrency, to: SupportedCurrency): Double {
            if (from == to) return amount
            val amountInUSD = amount * from.exchangeRateToUSD
            return amountInUSD / to.exchangeRateToUSD
        }
    }
}
