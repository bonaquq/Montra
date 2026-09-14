package com.example.util

object AmountInputUtils {
    /**
     * Sanitizes amount input so that ONLY digits and at most one decimal point are permitted.
     * Rejects all alphabetic characters, symbols, negative signs, emojis, commas, and limits decimal precision to 2 digits.
     */
    fun sanitizeAmount(input: String, maxDecimals: Int = 2): String {
        if (input.isEmpty()) return ""
        
        var hasDot = false
        var decimalCount = 0
        val sb = StringBuilder()

        for (ch in input) {
            if (ch.isDigit()) {
                if (hasDot) {
                    if (decimalCount < maxDecimals) {
                        sb.append(ch)
                        decimalCount++
                    }
                } else {
                    sb.append(ch)
                }
            } else if (ch == '.' && !hasDot) {
                sb.append(ch)
                hasDot = true
            }
        }
        return sb.toString()
    }

    /**
     * Checks if a new character sequence is a valid decimal amount.
     */
    fun isValidAmount(input: String): Boolean {
        if (input.isEmpty()) return true
        return input.matches(Regex("^\\d*\\.?\\d{0,2}$"))
    }
}
