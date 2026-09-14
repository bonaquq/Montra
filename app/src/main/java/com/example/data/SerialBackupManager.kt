package com.example.data

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class SerialBackupBundle(
    val serialKey: String,
    val authorName: String,
    val authorEmail: String,
    val timestamp: Long,
    val initialBalance: Double,
    val currencyCode: String = "MVR",
    val expenses: List<Expense>,
    val budgets: List<Budget>,
    val customCategories: List<CustomCategory>,
    val rawSelfContainedToken: String? = null
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(timestamp))

    val incomeCount: Int
        get() = expenses.count { it.isIncome }

    val expenseCount: Int
        get() = expenses.count { !it.isIncome }

    val totalBalanceSum: Double
        get() = initialBalance + expenses.sumOf { if (it.isIncome) it.amount else -it.amount }
}

object SerialBackupManager {
    private const val TAG = "SerialBackupManager"
    private const val CHARSET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val random = SecureRandom()

    /**
     * Generates a distinct, clean random serial number in the format MNTR-XXXX-XXXX-XXXX
     */
    fun generateRandomSerial(): String {
        fun randomSegment(length: Int): String {
            val sb = StringBuilder(length)
            for (i in 0 until length) {
                sb.append(CHARSET[random.nextInt(CHARSET.length)])
            }
            return sb.toString()
        }
        return "MNTR-${randomSegment(4)}-${randomSegment(4)}-${randomSegment(4)}"
    }

    /**
     * Sanitizes user input for serial search
     */
    fun normalizeSerialKey(input: String): String {
        val trimmed = input.trim().uppercase(Locale.US)
        if (trimmed.startsWith("MNTRDATA:")) {
            return trimmed
        }
        // If it's formatted or unformatted like MNTR-XXXX-XXXX-XXXX or MNTRXXXXXXXXXXXX
        val cleaned = trimmed.replace(Regex("[^A-Z0-9]"), "")
        return if (cleaned.startsWith("MNTR") && cleaned.length >= 16) {
            val s = cleaned.substring(4)
            "MNTR-${s.substring(0, 4)}-${s.substring(4, 8)}-${s.substring(8, 12)}"
        } else {
            trimmed
        }
    }

    /**
     * Encodes full backup bundle into a JSON String
     */
    fun bundleToJson(bundle: SerialBackupBundle): String {
        val root = JSONObject()
        root.put("serialKey", bundle.serialKey)
        root.put("authorName", bundle.authorName)
        root.put("authorEmail", bundle.authorEmail)
        root.put("timestamp", bundle.timestamp)
        root.put("initialBalance", bundle.initialBalance)
        root.put("currencyCode", bundle.currencyCode)

        // Expenses
        val expensesArray = JSONArray()
        bundle.expenses.forEach { exp ->
            val obj = JSONObject()
            obj.put("id", exp.id)
            obj.put("title", exp.title)
            obj.put("amount", exp.amount)
            obj.put("category", exp.category)
            obj.put("dateMillis", exp.dateMillis)
            obj.put("note", exp.note)
            obj.put("currencyCode", exp.currencyCode)
            obj.put("receiptUri", exp.receiptUri ?: "")
            obj.put("isAutomated", exp.isAutomated)
            obj.put("transactionType", exp.transactionType)
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        // Budgets
        val budgetsArray = JSONArray()
        bundle.budgets.forEach { b ->
            val obj = JSONObject()
            obj.put("category", b.category)
            obj.put("monthlyLimit", b.monthlyLimit)
            obj.put("currencyCode", b.currencyCode)
            budgetsArray.put(obj)
        }
        root.put("budgets", budgetsArray)

        // Custom categories
        val categoriesArray = JSONArray()
        bundle.customCategories.forEach { cat ->
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("iconKey", cat.iconKey)
            obj.put("colorHex", cat.colorHex)
            categoriesArray.put(obj)
        }
        root.put("customCategories", categoriesArray)

        return root.toString()
    }

    /**
     * Decodes JSON String into a SerialBackupBundle
     */
    fun jsonToBundle(jsonString: String, explicitSerial: String? = null): SerialBackupBundle {
        val root = JSONObject(jsonString)
        val serialKey = explicitSerial ?: root.optString("serialKey", generateRandomSerial())
        val authorName = root.optString("authorName", "Montra User")
        val authorEmail = root.optString("authorEmail", "")
        val timestamp = root.optLong("timestamp", System.currentTimeMillis())
        val initialBalance = root.optDouble("initialBalance", 0.0)
        val currencyCode = root.optString("currencyCode", "MVR")

        val expenses = mutableListOf<Expense>()
        val expensesArray = root.optJSONArray("expenses") ?: JSONArray()
        for (i in 0 until expensesArray.length()) {
            val obj = expensesArray.getJSONObject(i)
            expenses.add(
                Expense(
                    id = obj.optLong("id", 0L),
                    title = obj.optString("title", "Expense"),
                    amount = obj.optDouble("amount", 0.0),
                    category = obj.optString("category", "OTHER"),
                    dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                    note = obj.optString("note", ""),
                    currencyCode = obj.optString("currencyCode", currencyCode),
                    receiptUri = obj.optString("receiptUri").takeIf { it.isNotEmpty() },
                    isAutomated = obj.optBoolean("isAutomated", false),
                    transactionType = obj.optString("transactionType", "EXPENSE")
                )
            )
        }

        val budgets = mutableListOf<Budget>()
        val budgetsArray = root.optJSONArray("budgets") ?: JSONArray()
        for (i in 0 until budgetsArray.length()) {
            val obj = budgetsArray.getJSONObject(i)
            budgets.add(
                Budget(
                    category = obj.optString("category", "OVERALL"),
                    monthlyLimit = obj.optDouble("monthlyLimit", 0.0),
                    currencyCode = obj.optString("currencyCode", currencyCode)
                )
            )
        }

        val customCategories = mutableListOf<CustomCategory>()
        val categoriesArray = root.optJSONArray("customCategories") ?: JSONArray()
        for (i in 0 until categoriesArray.length()) {
            val obj = categoriesArray.getJSONObject(i)
            customCategories.add(
                CustomCategory(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", "Category"),
                    iconKey = obj.optString("iconKey", "Category"),
                    colorHex = obj.optString("colorHex", "#2563EB")
                )
            )
        }

        return SerialBackupBundle(
            serialKey = serialKey,
            authorName = authorName,
            authorEmail = authorEmail,
            timestamp = timestamp,
            initialBalance = initialBalance,
            currencyCode = currencyCode,
            expenses = expenses,
            budgets = budgets,
            customCategories = customCategories
        )
    }

    /**
     * Compresses and encodes JSON to a self-contained portable token string
     */
    fun compressBundleToToken(jsonString: String): String {
        return try {
            val bos = ByteArrayOutputStream()
            GZIPOutputStream(bos).use { gzip ->
                gzip.write(jsonString.toByteArray(Charsets.UTF_8))
            }
            "MNTRDATA:" + Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        } catch (e: Exception) {
            Log.e(TAG, "Compression error", e)
            "MNTRDATA:" + Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
        }
    }

    /**
     * Decodes a self-contained token string back into JSON
     */
    fun decompressTokenToJson(token: String): String? {
        return try {
            val raw = if (token.startsWith("MNTRDATA:")) token.removePrefix("MNTRDATA:") else token
            val bytes = Base64.decode(raw, Base64.NO_WRAP or Base64.URL_SAFE)
            try {
                val bis = ByteArrayInputStream(bytes)
                GZIPInputStream(bis).bufferedReader(Charsets.UTF_8).use { it.readText() }
            } catch (e: Exception) {
                String(bytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Decompression error", e)
            null
        }
    }

    /**
     * Exports and publishes the backup bundle to Firestore and local preferences
     */
    suspend fun exportSerialBackup(
        context: Context,
        bundle: SerialBackupBundle
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = bundleToJson(bundle)
            val token = compressBundleToToken(jsonString)

            // Save to Firestore under "serial_backups" collection
            try {
                val firestore = FirebaseFirestore.getInstance()
                val firestoreData = hashMapOf(
                    "serialKey" to bundle.serialKey,
                    "authorName" to bundle.authorName,
                    "authorEmail" to bundle.authorEmail,
                    "timestamp" to bundle.timestamp,
                    "initialBalance" to bundle.initialBalance,
                    "currencyCode" to bundle.currencyCode,
                    "expensesCount" to bundle.expenses.size,
                    "budgetsCount" to bundle.budgets.size,
                    "categoriesCount" to bundle.customCategories.size,
                    "dataJson" to jsonString,
                    "token" to token
                )
                firestore.collection("serial_backups")
                    .document(bundle.serialKey)
                    .set(firestoreData, SetOptions.merge())
                    .await()
                Log.d(TAG, "Serial backup uploaded successfully to Firestore for key: ${bundle.serialKey}")
            } catch (fe: Exception) {
                Log.w(TAG, "Firestore export fallback (offline mode): ${fe.message}")
            }

            // Save serial key in local exported history
            saveExportHistory(context, bundle.serialKey, bundle.expenses.size, bundle.totalBalanceSum)

            Result.success(bundle.serialKey)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export serial backup", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches and parses a Serial Backup by its serial number or direct token
     */
    suspend fun fetchSerialBackup(
        context: Context,
        serialInput: String
    ): Result<SerialBackupBundle> = withContext(Dispatchers.IO) {
        val normalized = normalizeSerialKey(serialInput)
        if (normalized.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Serial number cannot be empty"))
        }

        // 1. Check if user pasted a self-contained data token
        if (normalized.startsWith("MNTRDATA:") || normalized.length > 80) {
            val json = decompressTokenToJson(normalized)
            if (json != null) {
                try {
                    val bundle = jsonToBundle(json)
                    return@withContext Result.success(bundle)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse token payload", e)
                }
            }
        }

        // 2. Query Firestore collection "serial_backups"
        try {
            val firestore = FirebaseFirestore.getInstance()
            val doc = firestore.collection("serial_backups")
                .document(normalized)
                .get()
                .await()

            if (doc.exists()) {
                val dataJson = doc.getString("dataJson")
                if (!dataJson.isNullOrBlank()) {
                    val bundle = jsonToBundle(dataJson, normalized)
                    return@withContext Result.success(bundle)
                }
            }
        } catch (fe: Exception) {
            Log.w(TAG, "Firestore fetch error: ${fe.message}")
        }

        // 3. Fallback: Check local history cache in SharedPreferences if available
        val prefs = context.getSharedPreferences("montra_serial_cache", Context.MODE_PRIVATE)
        val cachedJson = prefs.getString("cache_$normalized", null)
        if (!cachedJson.isNullOrBlank()) {
            try {
                val bundle = jsonToBundle(cachedJson, normalized)
                return@withContext Result.success(bundle)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse cached JSON", e)
            }
        }

        Result.failure(NoSuchElementException("No backup data found for Serial Number: $normalized. Please verify the code and ensure it was exported while online."))
    }

    private fun saveExportHistory(context: Context, serialKey: String, itemCount: Int, balance: Double) {
        try {
            val prefs = context.getSharedPreferences("montra_serial_history", Context.MODE_PRIVATE)
            val history = prefs.getStringSet("keys", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            val entry = "$serialKey|$itemCount|$balance|${System.currentTimeMillis()}"
            history.add(entry)
            prefs.edit().putStringSet("keys", history).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving export history", e)
        }
    }

    fun getExportHistory(context: Context): List<ExportHistoryItem> {
        return try {
            val prefs = context.getSharedPreferences("montra_serial_history", Context.MODE_PRIVATE)
            val history = prefs.getStringSet("keys", emptySet()) ?: emptySet()
            history.mapNotNull { raw ->
                val parts = raw.split("|")
                if (parts.size >= 4) {
                    ExportHistoryItem(
                        serialKey = parts[0],
                        itemCount = parts[1].toIntOrNull() ?: 0,
                        balance = parts[2].toDoubleOrNull() ?: 0.0,
                        timestamp = parts[3].toLongOrNull() ?: 0L
                    )
                } else null
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class ExportHistoryItem(
    val serialKey: String,
    val itemCount: Int,
    val balance: Double,
    val timestamp: Long
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}
