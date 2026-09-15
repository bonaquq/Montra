package com.example.data

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
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
    private const val PREFS_CACHE = "montra_serial_cache"
    private const val PREFS_HISTORY = "montra_serial_history"
    private const val CHARSET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val random = SecureRandom()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Generates a distinct random fallback serial number in the format MNTR-XXXX-XXXX-XXXX
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
     * Formats an alphanumeric cloud key into standard MNTR serial format
     */
    fun formatAsSerialKey(rawId: String): String {
        val clean = rawId.replace(Regex("[^A-Za-z0-9]"), "").uppercase(Locale.US)
        return when {
            clean.startsWith("MNTR") && clean.length > 8 -> {
                val rem = clean.removePrefix("MNTR")
                if (rem.length >= 8) "MNTR-${rem.take(4)}-${rem.drop(4)}" else "MNTR-$rem"
            }
            clean.length in 8..10 -> "MNTR-${clean.take(4)}-${clean.drop(4)}"
            clean.length > 10 -> "MNTR-${clean.take(4)}-${clean.substring(4, 8)}-${clean.drop(8)}"
            clean.isNotEmpty() -> "MNTR-$clean"
            else -> generateRandomSerial()
        }
    }

    /**
     * Extracts raw cloud or storage ID from user input (handles URLs, formatted serials, etc.)
     */
    fun extractRawId(input: String): String {
        var str = input.trim()
        if (str.contains("dpaste.com/")) {
            str = str.substringAfter("dpaste.com/").substringBefore(".txt").substringBefore("?").substringBefore("/")
        } else if (str.contains("paste.rs/")) {
            str = str.substringAfter("paste.rs/").substringBefore("?").substringBefore("/")
        }
        val clean = str.replace(Regex("[^A-Za-z0-9]"), "")
        return if (clean.startsWith("MNTR", ignoreCase = true) && clean.length > 4) {
            clean.substring(4)
        } else {
            clean
        }
    }

    /**
     * Sanitizes user input for serial search
     */
    fun normalizeSerialKey(input: String): String {
        val trimmed = input.trim().uppercase(Locale.US)
        if (trimmed.startsWith("MNTRDATA:")) {
            return trimmed
        }
        val rawId = extractRawId(trimmed)
        return if (rawId.isNotEmpty()) formatAsSerialKey(rawId) else trimmed
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
     * Caches bundle locally under multiple lookup keys so it is always retrievable immediately
     */
    fun cacheBundleLocally(context: Context, bundle: SerialBackupBundle, rawId: String? = null) {
        try {
            val jsonString = bundleToJson(bundle)
            val prefs = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("cache_${bundle.serialKey}", jsonString)
            editor.putString("cache_${normalizeSerialKey(bundle.serialKey)}", jsonString)
            editor.putString("cache_${bundle.serialKey.replace("-", "").uppercase(Locale.US)}", jsonString)
            val id = rawId ?: extractRawId(bundle.serialKey)
            if (id.isNotEmpty()) {
                editor.putString("cache_$id", jsonString)
                editor.putString("cache_${id.uppercase(Locale.US)}", jsonString)
                editor.putString("cache_${id.lowercase(Locale.US)}", jsonString)
            }
            editor.putString("cache_latest", jsonString)
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error caching backup bundle locally", e)
        }
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
     * Exports and publishes the backup bundle to Cloud and local persistence
     * Returns the finalized random serial key.
     */
    suspend fun exportSerialBackup(
        context: Context,
        bundle: SerialBackupBundle
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            var activeSerial = bundle.serialKey
            var rawCloudId: String? = null
            val initialJson = bundleToJson(bundle)

            // Cache immediately to guarantee local availability even if offline
            cacheBundleLocally(context, bundle)

            // Try Cloud Upload to dpaste.com API v2 (zero-config, high availability)
            try {
                val formBody = FormBody.Builder()
                    .add("content", initialJson)
                    .add("expiry_days", "365")
                    .add("title", "Montra Backup ${bundle.serialKey}")
                    .build()

                val request = Request.Builder()
                    .url("https://dpaste.com/api/v2/")
                    .header("User-Agent", "MontraExpenseTracker/2.0")
                    .post(formBody)
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val url = response.body?.string()?.trim() ?: ""
                    val cloudId = url.substringAfterLast("/").trim()
                    if (cloudId.isNotEmpty() && cloudId.length in 5..15) {
                        rawCloudId = cloudId.uppercase(Locale.US)
                        activeSerial = formatAsSerialKey(rawCloudId)
                        Log.d(TAG, "Uploaded to dpaste successfully: $activeSerial (cloudId: $cloudId)")
                    }
                }
            } catch (ce: Exception) {
                Log.w(TAG, "dpaste cloud upload failed: ${ce.message}")
            }

            // Update bundle with final active serial
            val finalizedBundle = bundle.copy(serialKey = activeSerial)
            val finalJson = bundleToJson(finalizedBundle)

            // Re-cache with the final cloud serial key and raw ID
            cacheBundleLocally(context, finalizedBundle, rawCloudId)

            // Also upload to Firestore if Firebase is active
            try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    val firestore = FirebaseFirestore.getInstance()
                    val firestoreData = hashMapOf(
                        "serialKey" to activeSerial,
                        "authorName" to finalizedBundle.authorName,
                        "authorEmail" to finalizedBundle.authorEmail,
                        "timestamp" to finalizedBundle.timestamp,
                        "initialBalance" to finalizedBundle.initialBalance,
                        "currencyCode" to finalizedBundle.currencyCode,
                        "expensesCount" to finalizedBundle.expenses.size,
                        "budgetsCount" to finalizedBundle.budgets.size,
                        "dataJson" to finalJson
                    )
                    firestore.collection("serial_backups")
                        .document(activeSerial)
                        .set(firestoreData, SetOptions.merge())
                        .await()
                    Log.d(TAG, "Serial backup uploaded to Firestore for key: $activeSerial")
                }
            } catch (fe: Exception) {
                Log.w(TAG, "Firestore sync skipped: ${fe.message}")
            }

            // Save serial key in local exported history
            saveExportHistory(context, activeSerial, finalizedBundle.expenses.size, finalizedBundle.totalBalanceSum)

            Result.success(activeSerial)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export serial backup", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches and parses a Serial Backup by its serial number, cloud code, or direct token
     */
    suspend fun fetchSerialBackup(
        context: Context,
        serialInput: String
    ): Result<SerialBackupBundle> = withContext(Dispatchers.IO) {
        val trimmed = serialInput.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Serial number cannot be empty"))
        }

        // 1. Direct compressed token check
        if (trimmed.startsWith("MNTRDATA:") || trimmed.length > 80) {
            val json = decompressTokenToJson(trimmed)
            if (json != null) {
                try {
                    val bundle = jsonToBundle(json)
                    cacheBundleLocally(context, bundle)
                    return@withContext Result.success(bundle)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse token payload", e)
                }
            }
        }

        val rawId = extractRawId(trimmed)
        val normalized = normalizeSerialKey(trimmed)

        // 2. Check local SharedPreferences cache
        val prefs = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
        val lookupKeys = listOf(
            "cache_$trimmed",
            "cache_${trimmed.uppercase(Locale.US)}",
            "cache_$normalized",
            "cache_$rawId",
            "cache_${rawId.uppercase(Locale.US)}",
            "cache_${rawId.lowercase(Locale.US)}",
            "cache_MNTR-$rawId",
            "cache_${trimmed.replace("-", "").uppercase(Locale.US)}"
        )

        for (key in lookupKeys) {
            val cachedJson = prefs.getString(key, null)
            if (!cachedJson.isNullOrBlank()) {
                try {
                    val bundle = jsonToBundle(cachedJson, normalized)
                    return@withContext Result.success(bundle)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed parsing cached json for key $key", e)
                }
            }
        }

        // 3. Query Cloud (dpaste.com)
        if (rawId.isNotEmpty()) {
            val cloudUrlsToTry = listOf(
                "https://dpaste.com/${rawId}.txt",
                "https://dpaste.com/${rawId.lowercase(Locale.US)}.txt",
                "https://dpaste.com/${rawId.uppercase(Locale.US)}.txt",
                "https://paste.rs/$rawId"
            )

            for (url in cloudUrlsToTry) {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "MontraExpenseTracker/2.0")
                        .get()
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()?.trim() ?: ""
                        if (body.startsWith("{") && (body.contains("\"expenses\"") || body.contains("\"serialKey\""))) {
                            val bundle = jsonToBundle(body, normalized)
                            cacheBundleLocally(context, bundle, rawId)
                            Log.d(TAG, "Successfully fetched cloud serial backup from $url")
                            return@withContext Result.success(bundle)
                        }
                    }
                } catch (ce: Exception) {
                    Log.w(TAG, "Cloud fetch attempt failed for $url: ${ce.message}")
                }
            }
        }

        // 4. Query Firestore if active
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val firestore = FirebaseFirestore.getInstance()
                val firestoreDocs = listOf(normalized, trimmed, rawId)
                for (docId in firestoreDocs) {
                    val doc = firestore.collection("serial_backups").document(docId).get().await()
                    if (doc.exists()) {
                        val dataJson = doc.getString("dataJson")
                        if (!dataJson.isNullOrBlank()) {
                            val bundle = jsonToBundle(dataJson, normalized)
                            cacheBundleLocally(context, bundle, rawId)
                            return@withContext Result.success(bundle)
                        }
                    }
                }
            }
        } catch (fe: Exception) {
            Log.w(TAG, "Firestore query error: ${fe.message}")
        }

        // 5. Fallback: check device latest backup if user has exported on this device
        val cachedLatest = prefs.getString("cache_latest", null)
        val history = getExportHistory(context)
        val matchesHistory = history.any {
            it.serialKey.equals(trimmed, ignoreCase = true) ||
            extractRawId(it.serialKey).equals(rawId, ignoreCase = true)
        }
        if (matchesHistory && !cachedLatest.isNullOrBlank()) {
            try {
                val bundle = jsonToBundle(cachedLatest, normalized)
                return@withContext Result.success(bundle)
            } catch (_: Exception) {}
        }

        Result.failure(
            NoSuchElementException(
                "No backup found for serial number \"$trimmed\".\nPlease ensure the serial number was exported from the Export tab, or verify the code."
            )
        )
    }

    private fun saveExportHistory(context: Context, serialKey: String, itemCount: Int, balance: Double) {
        try {
            val prefs = context.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE)
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
            val prefs = context.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE)
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
        get() = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
