package com.example.data

enum class SupportedCurrency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val exchangeRateToUSD: Double // 1 unit of this currency = exchangeRateToUSD in USD
) {
    USD("USD", "$", "US Dollar", 1.0),                  // 1 USD = 15.42 MVR
    EUR("EUR", "€", "Euro", 17.94 / 15.42),             // 1 EUR = 17.94 MVR
    GBP("GBP", "£", "British Pound", 20.90 / 15.42),     // 1 GBP = 20.90 MVR
    JPY("JPY", "¥", "Japanese Yen", 0.10 / 15.42),       // 1 JPY = 0.10 MVR
    INR("INR", "₹", "Indian Rupee", 0.16 / 15.42),       // 1 INR = 0.16 MVR
    CAD("CAD", "CA$", "Canadian Dollar", 11.12 / 15.42), // 1 CAD = 11.12 MVR
    AUD("AUD", "A$", "Australian Dollar", 11.04 / 15.42),// 1 AUD = 11.04 MVR
    MVR("MVR", "Rf", "Maldivian Rufiyaa", 1.0 / 15.42);  // 1 MVR = 1.0 MVR (Base: 1 USD = 15.42 MVR)

    companion object {
        fun fromCode(code: String?): SupportedCurrency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: MVR
        }

        fun convert(amount: Double, from: SupportedCurrency, to: SupportedCurrency): Double {
            if (from == to) return amount
            val amountInUSD = amount * from.exchangeRateToUSD
            return amountInUSD / to.exchangeRateToUSD
        }
    }
}
