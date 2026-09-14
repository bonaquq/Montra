package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class ParsedReceiptData(
    val merchantOrTitle: String,
    val amount: Double,
    val categoryHint: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val rawNotes: String = "",
    val bankName: String? = null,
    val referenceNo: String? = null,
    val isCreditOrIncome: Boolean = false,
    val currencyCode: String = "MVR"
)

object ReceiptParser {

    /**
     * Parses a receipt or bank statement image using on-device ML Kit OCR
     * and Gemini Vision AI for maximum accuracy across Bank of Maldives (BML),
     * Maldives Islamic Bank (MIB), utility bills, and merchant receipts.
     */
    suspend fun parseReceiptImage(
        context: Context,
        imageUri: Uri,
        apiKey: String
    ): ParsedReceiptData = withContext(Dispatchers.IO) {
        val bitmap = loadScaledBitmap(context, imageUri)

        // 1. Try Gemini Vision if API key is provided
        if (bitmap != null && apiKey.isNotBlank() && !apiKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            try {
                val geminiResult = callGeminiVision(bitmap, apiKey)
                if (geminiResult != null && (geminiResult.amount > 0.0 || geminiResult.merchantOrTitle.isNotBlank())) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. High-precision on-device ML Kit OCR on the actual scanned image
        if (bitmap != null) {
            val ocrText = runOnDeviceOcr(bitmap)
            if (!ocrText.isNullOrBlank()) {
                val ocrResult = parseReceiptOcrText(ocrText)
                return@withContext ocrResult
            }
        }

        // 3. Fallback with zero amount so user can review and input exact details
        ParsedReceiptData(
            merchantOrTitle = "Scanned Document",
            amount = 0.0,
            categoryHint = "OTHER",
            dateMillis = System.currentTimeMillis(),
            rawNotes = "Scanned document (please review details)",
            bankName = null,
            referenceNo = null,
            isCreditOrIncome = false,
            currencyCode = "MVR"
        )
    }

    private suspend fun runOnDeviceOcr(bitmap: Bitmap): String? = suspendCancellableCoroutine { continuation ->
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (continuation.isActive) {
                        continuation.resume(visionText.text)
                    }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }

    private fun loadScaledBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            val original = BitmapFactory.decodeStream(input)
            input?.close()
            if (original == null) return null

            val maxDimension = 1280
            if (original.width > maxDimension || original.height > maxDimension) {
                val ratio = original.width.toFloat() / original.height.toFloat()
                val targetW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(original, targetW, targetH, true)
            } else {
                original
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun callGeminiVision(bitmap: Bitmap, apiKey: String): ParsedReceiptData? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

        val prompt = """
            You are an expert financial document, receipt, and bank statement OCR parser.
            Examine this user-provided image (such as a Bank of Maldives BML slip, Maldives Islamic Bank MIB slip, utility bill, invoice, or store receipt).

            CRITICAL EXTRACTION RULES:
            1. AMOUNT:
               - Extract the EXACT primary transaction amount shown on THIS image.
               - Look for prominent large figures, amount rows, "Transfer successful" amount, "Total", "MVR", "USD", "Rf", "MRF", or "Amount".
               - Return the exact numeric value found in THIS image. Never guess or substitute a standard default amount.
               - Return as positive double in "amount".

            2. TRANSACTION TYPE & COLOR / SIGN INDICATOR:
               - If the amount is red or prefixed with minus "-" or indicates a POS store purchase / payment:
                 "category": "PURCHASE" or store category, "transactionType": "EXPENSE", "isCreditOrIncome": false.
               - If it indicates a transfer to recipient or green indicator / income / deposit:
                 "category": "TRANSFER" or "INCOME", "transactionType": "INCOME", "isCreditOrIncome": true.

            3. MERCHANT / BENEFICIARY / DESCRIPTION:
               - Extract the actual clean merchant name, paid-to beneficiary, utility provider, or store name from THIS slip.
               - Clean out transaction timestamp prefixes, authorization codes, or trailing branch codes.

            4. DETAILS:
               - Extract Transaction ID, Reference Number, and Date (in YYYY-MM-DD format).

            Return purely a valid JSON object without markdown formatting or code blocks:
            {
              "bankName": "Bank of Maldives" | "Maldives Islamic Bank" | null,
              "merchant": "Actual extracted merchant/beneficiary name",
              "amount": 0.0,
              "currency": "MVR",
              "isCreditOrIncome": false,
              "transactionType": "EXPENSE" | "INCOME",
              "referenceNo": "Actual extracted reference or txn id",
              "date": "YYYY-MM-DD",
              "category": "PURCHASE" | "TRANSFER" | "FOOD" | "SHOPPING" | "UTILITIES" | "TRANSPORT" | "RENT" | "HEALTH" | "INCOME" | "OTHER",
              "notes": "Brief notes from slip"
            }
        """.trimIndent()

        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.doOutput = true

        val requestBody = JSONObject().apply {
            val contents = org.json.JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = org.json.JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        conn.outputStream.use { os ->
            os.write(requestBody.toString().toByteArray())
        }

        if (conn.responseCode == 200) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    val textPart = parts.getJSONObject(0).getString("text")
                    val cleanJson = textPart.replace("```json", "").replace("```", "").trim()
                    val parsed = JSONObject(cleanJson)
                    val dateStr = parsed.optString("date", "")
                    val parsedDateMillis = parseDateStringToMillis(dateStr)
                    val bank = parsed.optString("bankName", "").takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
                    val rawCat = parsed.optString("category", "OTHER")
                    val category = if (rawCat.equals("BILLS", ignoreCase = true)) "UTILITIES" else rawCat
                    val isCredit = parsed.optBoolean("isCreditOrIncome", false)
                    val currency = parsed.optString("currency", "MVR").uppercase()
                    val ref = parsed.optString("referenceNo", "").takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
                    val extractedMerchant = parsed.optString("merchant", "").trim()
                    val extractedAmount = parsed.optDouble("amount", 0.0)

                    return ParsedReceiptData(
                        merchantOrTitle = extractedMerchant.ifBlank { bank?.let { "$it Transaction" } ?: "Scanned Receipt" },
                        amount = extractedAmount,
                        categoryHint = category,
                        dateMillis = parsedDateMillis,
                        rawNotes = parsed.optString("notes", bank?.let { "$it slip" } ?: "Scanned document"),
                        bankName = bank,
                        referenceNo = ref,
                        isCreditOrIncome = isCredit,
                        currencyCode = currency
                    )
                }
            }
        }
        return null
    }

    private fun parseDateStringToMillis(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        val formats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US),
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US),
            java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US),
            java.text.SimpleDateFormat("dd-MMM-yyyy", java.util.Locale.US),
            java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.US),
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
        )
        for (format in formats) {
            try {
                val date = format.parse(dateStr.trim())
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    /**
     * Optical Character Recognition (OCR) text parser:
     * Extracts exact details from scanned text of BML/MIB statements, bills, or store receipts.
     */
    fun parseReceiptOcrText(ocrText: String): ParsedReceiptData {
        val lower = ocrText.lowercase()

        val isBml = lower.contains("bank of maldives") || lower.contains("bml") ||
                lower.contains("bml mobilepay") || lower.contains("bml internet") || lower.contains("transfer successful")
        val isMib = lower.contains("maldives islamic bank") || lower.contains("mib") ||
                lower.contains("faisamobile") || lower.contains("faisanet") || lower.contains("faisapay")

        if (isBml || isMib) {
            return parseBankStatementText(ocrText, if (isBml) "Bank of Maldives" else "Maldives Islamic Bank")
        }

        val isBill = lower.contains("stelco") || lower.contains("mwsc") || lower.contains("dhiraagu") ||
                lower.contains("ooredoo") || lower.contains("medianet") || lower.contains("wamco") ||
                lower.contains("electricity bill") || lower.contains("water bill") || lower.contains("utility bill") ||
                lower.contains("tax invoice") || lower.contains("bill statement") || lower.contains("bill payment")

        if (isBill) {
            return parseUtilityBillText(ocrText)
        }

        // Standard receipt parsing
        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var merchant = ""
        var amount = 0.0
        var dateMillis = System.currentTimeMillis()

        for (line in lines.take(5)) {
            val l = line.lowercase()
            if (!l.contains("receipt") && !l.contains("welcome") && !l.contains("tel:") && !l.contains("tax invoice") && !l.contains("cashier") && !l.contains("date")) {
                val clean = line.replace(Regex("[^A-Za-z0-9'&.\\s]"), "").trim()
                if (clean.length > 2) {
                    merchant = clean
                    break
                }
            }
        }

        val totalPattern = Pattern.compile("(?i)(?:TOTAL|AMOUNT DUE|BALANCE DUE|SUBTOTAL|CHARGED|GRAND TOTAL)[\\s:]*(?:MVR|Rf|MRF|USD|[$€£₹])?\\s*([0-9,]+(?:\\.[0-9]{2})?)")
        var foundTotal = false
        for (line in lines.reversed()) {
            val matcher = totalPattern.matcher(line)
            if (matcher.find()) {
                val clean = matcher.group(1)?.replace(",", "")
                val matchedVal = clean?.toDoubleOrNull()
                if (matchedVal != null && matchedVal > 0) {
                    amount = matchedVal
                    foundTotal = true
                    break
                }
            }
        }

        if (!foundTotal) {
            val pricePattern = Pattern.compile("(?:MVR|Rf|MRF|USD|[$€£₹])?\\s*([0-9,]+\\.[0-9]{2})")
            val amounts = mutableListOf<Double>()
            for (line in lines) {
                val m = pricePattern.matcher(line)
                while (m.find()) {
                    val clean = m.group(1)?.replace(",", "")
                    clean?.toDoubleOrNull()?.let { amounts.add(it) }
                }
            }
            if (amounts.isNotEmpty()) {
                amount = amounts.maxOrNull() ?: 0.0
            }
        }

        val datePattern = Pattern.compile("(\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}|\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}|[A-Za-z]{3,9}\\s+\\d{1,2},?\\s+\\d{4})")
        for (line in lines) {
            val m = datePattern.matcher(line)
            if (m.find()) {
                val rawDate = m.group(1) ?: ""
                val parsed = parseDateStringToMillis(rawDate)
                if (parsed > 0) {
                    dateMillis = parsed
                    break
                }
            }
        }

        val category = com.example.data.ExpenseCategory.predictCategory("$merchant $ocrText").name

        return ParsedReceiptData(
            merchantOrTitle = merchant.ifBlank { "Scanned Purchase" },
            amount = amount,
            categoryHint = category,
            dateMillis = dateMillis,
            rawNotes = if (merchant.isNotBlank()) "Scanned: $merchant" else "Scanned document"
        )
    }

    /**
     * Parser for Utility and Service Bills (STELCO, MWSC, Dhiraagu, Ooredoo, Medianet, WAMCO, Invoices)
     */
    fun parseUtilityBillText(text: String): ParsedReceiptData {
        val lower = text.lowercase()
        val provider = when {
            lower.contains("stelco") || lower.contains("electricity") -> "STELCO Electricity Bill"
            lower.contains("mwsc") || lower.contains("water") -> "MWSC Water Bill"
            lower.contains("dhiraagu") -> "Dhiraagu Telecom / Internet Bill"
            lower.contains("ooredoo") -> "Ooredoo Postpaid / Fiber Bill"
            lower.contains("medianet") -> "Medianet Cable TV Bill"
            lower.contains("wamco") -> "WAMCO Waste Management Bill"
            lower.contains("tax invoice") -> "Tax Invoice Bill"
            else -> "Utility Bill Statement"
        }

        // 1. Amount Due / Total Payable
        var billAmount = 0.0
        val billAmountPatterns = listOf(
            Pattern.compile("(?i)(?:Total\\s*Payable|Amount\\s*Due|Current\\s*Charges|Total\\s*Due|Bill\\s*Amount|Net\\s*Payable|Total)[\\s:]*(?:MVR|Rf|MRF|USD|\\$)?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?|[0-9]+(?:\\.[0-9]{2})?)"),
            Pattern.compile("(?i)(?:MVR|Rf|MRF|USD|\\$)\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?|[0-9]+(?:\\.[0-9]{2})?)"),
            Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})*\\.[0-9]{2})\\s*(?:MVR|Rf|MRF)")
        )
        for (pat in billAmountPatterns) {
            val m = pat.matcher(text)
            if (m.find()) {
                val clean = m.group(1)?.replace(",", "")
                val parsed = clean?.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    billAmount = parsed
                    break
                }
            }
        }

        // 2. Account No / Meter No / Invoice Ref
        val invPattern = Pattern.compile("(?i)(?:Account\\s*(?:No\\.?|Number)|Meter\\s*(?:No\\.?|Number)|Invoice\\s*(?:No\\.?|Number)|Bill\\s*(?:No\\.?|Number)|Ref\\s*No)[\\s:]*([A-Za-z0-9\\-_/]+)")
        val invMatcher = invPattern.matcher(text)
        val invoiceRef = if (invMatcher.find()) invMatcher.group(1) else null

        // 3. Date / Due Date
        var dateMillis = System.currentTimeMillis()
        val datePattern = Pattern.compile("(\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}|\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}|\\d{1,2}-[A-Za-z]{3}-\\d{2,4})")
        val dateMatcher = datePattern.matcher(text)
        if (dateMatcher.find()) {
            val rawDate = dateMatcher.group(1) ?: ""
            val parsed = parseDateStringToMillis(rawDate)
            if (parsed > 0) dateMillis = parsed
        }

        val notes = buildString {
            append(provider)
            if (invoiceRef != null) append(" • Ref: $invoiceRef")
        }

        return ParsedReceiptData(
            merchantOrTitle = provider,
            amount = billAmount,
            categoryHint = com.example.data.ExpenseCategory.UTILITIES.name,
            dateMillis = dateMillis,
            rawNotes = notes,
            bankName = null,
            referenceNo = invoiceRef,
            isCreditOrIncome = false,
            currencyCode = "MVR"
        )
    }

    /**
     * Specialized parser for Bank of Maldives (BML) and Maldives Islamic Bank (MIB)
     * statements, transfer advices, and SMS alerts.
     */
    fun parseBankStatementText(text: String, detectedBank: String? = null): ParsedReceiptData {
        val lower = text.lowercase()
        val bank = detectedBank ?: when {
            lower.contains("bank of maldives") || lower.contains("bml") -> "Bank of Maldives"
            lower.contains("maldives islamic bank") || lower.contains("mib") || lower.contains("faisa") -> "Maldives Islamic Bank"
            else -> "Bank Statement"
        }

        // 1. Amount Extraction (MVR / Rf / USD)
        var extractedAmount = 0.0
        var isNegativePurchase = false

        // Check for prominent middle-top amount under "Transfer successful"
        val topAmountPattern = Pattern.compile("(?i)Transfer\\s+successful[\\s\\n\\r]+(?:(?:MVR|Rf|MRF|USD|\\$)[\\s:]*)?([0-9,]+\\.[0-9]{2})")
        val topMatcher = topAmountPattern.matcher(text)
        if (topMatcher.find()) {
            val amt = topMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
            if (amt != null && amt > 0.0) {
                extractedAmount = amt
            }
        }

        // Check Amount field with negative sign (e.g. MVR -148.00 or -148.00)
        val negativeAmountPattern = Pattern.compile("(?i)(?:Amount|Total)?[\\s:]*(?:MVR|Rf|MRF|USD|\\$)?\\s*-\\s*([0-9,]+(?:\\.[0-9]{2})?)")
        val negMatcher = negativeAmountPattern.matcher(text)
        if (negMatcher.find()) {
            val amt = negMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
            if (amt != null && amt > 0.0) {
                if (extractedAmount == 0.0) extractedAmount = amt
                isNegativePurchase = true
            }
        }

        if (extractedAmount == 0.0) {
            val amountPatterns = listOf(
                Pattern.compile("(?i)(?:Amount|Total|Debit|Credit)[\\s:]*(?:MVR|Rf|MRF|USD|\\$)?\\s*(-?[0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?|-?[0-9]+(?:\\.[0-9]{2})?)"),
                Pattern.compile("(?i)(?:MVR|Rf|MRF|USD|\\$)\\s*(-?[0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?|-?[0-9]+(?:\\.[0-9]{2})?)"),
                Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})*\\.[0-9]{2})\\s*(?:MVR|Rf|MRF)")
            )
            for (pat in amountPatterns) {
                val m = pat.matcher(text)
                if (m.find()) {
                    val rawMatch = m.group(1)?.replace(",", "") ?: ""
                    if (rawMatch.startsWith("-")) {
                        isNegativePurchase = true
                    }
                    val parsed = rawMatch.replace("-", "").toDoubleOrNull()
                    if (parsed != null && parsed > 0.0) {
                        extractedAmount = parsed
                        break
                    }
                }
            }
        }

        // 2. Reference / Transaction ID Extraction
        val refPattern = Pattern.compile("(?i)(?:Reference(?:\\s*No\\.?|\\s*ID)?|Ref\\s*(?:No\\.?|ID)?|Txn\\s*Ref|Journal\\s*No)[\\s:]*([A-Za-z0-9\\-_\\\\]+)")
        val refMatcher = refPattern.matcher(text)
        val referenceNo = if (refMatcher.find()) refMatcher.group(1) else null

        val txnIdPattern = Pattern.compile("(?i)Transaction\\s*ID[\\s:]*([A-Za-z0-9\\-_]+)")
        val txnMatcher = txnIdPattern.matcher(text)
        val transactionId = if (txnMatcher.find()) txnMatcher.group(1) else null

        // 3. Counterparty / Recipient / Description / Narration
        var counterparty: String? = null

        val descPattern = Pattern.compile("(?i)(?:Description|Remarks?|Narration|Purpose)[\\s:]+([^\\n\\r]+)")
        val descMatcher = descPattern.matcher(text)
        if (descMatcher.find()) {
            val rawDesc = descMatcher.group(1)?.trim() ?: ""
            var cleaned = rawDesc
                .replace(Regex("^\\d{2}[-/.]\\d{2}[-/.]\\d{4}\\s+"), "") // remove leading date
                .replace(Regex("^\\d{2}[-/.]\\d{2}[-/.]\\d{2}\\s+"), "") // remove leading time
                .replace(Regex("^\\d{6,}\\s+"), "") // remove authorization code
                .replace(Regex("\\s+(?:MALE|HULHUMALE|VILLIMALE|MV|MALDIVES|Internet Banking|BML MobilePay|MobilePay).*$", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\s+\\d{6,}$"), "") // remove trailing code
                .trim()
            if (cleaned.length >= 2) {
                counterparty = cleaned
            }
        }

        if (counterparty == null) {
            val counterpartyPatterns = listOf(
                Pattern.compile("(?i)(?:Transfer\\s*to|Paid\\s*to|Beneficiary(?:\\s*Name)?|Beneficiary|To\\s*Account|Merchant)[\\s:]+([A-Za-z0-9'&.\\-\\s]{2,40})(?:\\n|\$|\\s{2,}|\\.)"),
                Pattern.compile("(?i)(?:Paid\\s+at)[\\s:]+([A-Za-z0-9'&.\\-\\s]{2,40})(?:\\n|\$|\\s{2,}|\\.)")
            )
            for (pat in counterpartyPatterns) {
                val m = pat.matcher(text)
                if (m.find()) {
                    val found = m.group(1)?.trim()
                    if (!found.isNullOrBlank() && !found.equals("MVR", ignoreCase = true) && !found.equals("SUCCESS", ignoreCase = true)) {
                        counterparty = found
                        break
                    }
                }
            }
        }

        // 4. Direction & Category
        val isPurchase = isNegativePurchase || lower.contains("purchase") || lower.contains("pos purchase")
        val isTransfer = !isNegativePurchase && (lower.contains("transfer") || lower.contains("transferred") || lower.contains("internet banking") || lower.contains("fund transfer"))

        val isCredit = if (isNegativePurchase) {
            false
        } else if (lower.contains("salary") || lower.contains("deposit") || lower.contains("credited") || lower.contains("received from")) {
            true
        } else if (isTransfer) {
            true
        } else {
            false
        }

        val categoryHint = when {
            isNegativePurchase || isPurchase -> com.example.data.ExpenseCategory.PURCHASE.name
            isTransfer -> com.example.data.ExpenseCategory.TRANSFER.name
            isCredit -> com.example.data.ExpenseCategory.INCOME.name
            else -> com.example.data.ExpenseCategory.predictCategory("$counterparty $text").name
        }

        val title = when {
            counterparty != null -> counterparty
            categoryHint == com.example.data.ExpenseCategory.PURCHASE.name -> "Store Purchase"
            categoryHint == com.example.data.ExpenseCategory.TRANSFER.name -> "Fund Transfer"
            isCredit -> "$bank: Deposit"
            else -> "$bank: Transaction"
        }

        // 5. Date Extraction
        var dateMillis = System.currentTimeMillis()
        val datePattern = Pattern.compile("(?i)(?:Post\\s*date|Transaction\\s*date|Date)[\\s:]*(\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}|\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2})")
        val dateMatcher = datePattern.matcher(text)
        if (dateMatcher.find()) {
            val rawDate = dateMatcher.group(1) ?: ""
            val parsed = parseDateStringToMillis(rawDate)
            if (parsed > 0) dateMillis = parsed
        } else {
            val fallbackDatePattern = Pattern.compile("(\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}|\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2})")
            val fallbackMatcher = fallbackDatePattern.matcher(text)
            if (fallbackMatcher.find()) {
                val rawDate = fallbackMatcher.group(1) ?: ""
                val parsed = parseDateStringToMillis(rawDate)
                if (parsed > 0) dateMillis = parsed
            }
        }

        val notes = buildString {
            append(bank)
            if (referenceNo != null) append(" • Ref: $referenceNo")
            if (transactionId != null) append(" • Txn: $transactionId")
            if (counterparty != null) append(" • $counterparty")
        }

        return ParsedReceiptData(
            merchantOrTitle = title,
            amount = extractedAmount,
            categoryHint = categoryHint,
            dateMillis = dateMillis,
            rawNotes = notes,
            bankName = bank,
            referenceNo = referenceNo ?: transactionId,
            isCreditOrIncome = isCredit,
            currencyCode = "MVR"
        )
    }

    fun parseReceiptFallback(fileName: String): ParsedReceiptData {
        return ParsedReceiptData(
            merchantOrTitle = "Scanned Document",
            amount = 0.0,
            categoryHint = "OTHER",
            dateMillis = System.currentTimeMillis(),
            rawNotes = "Scanned document",
            bankName = null,
            referenceNo = null,
            isCreditOrIncome = false,
            currencyCode = "MVR"
        )
    }

    /**
     * Automated SMS / Bank Notification String Parser
     */
    fun parseAutomatedBankText(text: String): ParsedReceiptData {
        return parseBankStatementText(text)
    }
}
