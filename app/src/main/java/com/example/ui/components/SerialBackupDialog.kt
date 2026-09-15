package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExportHistoryItem
import com.example.data.SerialBackupBundle
import com.example.data.SerialBackupManager
import com.example.ui.ExpenseUiState
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.*
import com.example.util.FormatUtils

enum class SerialBackupTab {
    EXPORT,
    IMPORT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerialBackupDialog(
    uiState: ExpenseUiState,
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit,
    initialTab: SerialBackupTab = SerialBackupTab.IMPORT
) {
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    var activeTab by remember { mutableStateOf(initialTab) }

    // Export states
    var isExporting by remember { mutableStateOf(false) }
    var lastExportedSerial by remember { mutableStateOf<String?>(null) }
    var lastExportedBundle by remember { mutableStateOf<SerialBackupBundle?>(null) }
    var exportHistory by remember { mutableStateOf<List<ExportHistoryItem>>(emptyList()) }

    // Import states
    var serialInput by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var previewBundle by remember { mutableStateOf<SerialBackupBundle?>(null) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var importSuccessMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        exportHistory = viewModel.getExportHistory()
    }

    val performFetchForSerial: (String) -> Unit = { targetSerial ->
        val trimmed = targetSerial.trim()
        if (trimmed.isNotEmpty()) {
            serialInput = trimmed
            activeTab = SerialBackupTab.IMPORT
            importErrorMessage = null
            importSuccessMessage = null
            isFetching = true
            viewModel.fetchSerialBackup(trimmed) { res ->
                isFetching = false
                res.onSuccess { bundle ->
                    previewBundle = bundle
                }.onFailure { err ->
                    importErrorMessage = err.message ?: "Failed to find serial data"
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MontraBackground,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp),
                shape = CircleShape,
                color = MontraTextMuted.copy(alpha = 0.4f)
            ) {}
        },
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .testTag("serial_backup_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF3B82F6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Data Transfer",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                        Text(
                            text = "Import / Export via Random Serial Number",
                            fontSize = 12.sp,
                            color = MontraTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_serial_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MontraTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two-tab selector: Export Data / Import Data
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MontraSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Export Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                activeTab = SerialBackupTab.EXPORT
                                importErrorMessage = null
                                importSuccessMessage = null
                            }
                            .testTag("tab_export_data"),
                        color = if (activeTab == SerialBackupTab.EXPORT) MontraButtonBg else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Upload,
                                contentDescription = null,
                                tint = if (activeTab == SerialBackupTab.EXPORT) Color.White else MontraTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export Data",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (activeTab == SerialBackupTab.EXPORT) Color.White else MontraTextSecondary
                            )
                        }
                    }

                    // Import Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                activeTab = SerialBackupTab.IMPORT
                                importErrorMessage = null
                            }
                            .testTag("tab_import_data"),
                        color = if (activeTab == SerialBackupTab.IMPORT) MontraButtonBg else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                tint = if (activeTab == SerialBackupTab.IMPORT) Color.White else MontraTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Import Data",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (activeTab == SerialBackupTab.IMPORT) Color.White else MontraTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Area
            Box(modifier = Modifier.weight(1f)) {
                if (activeTab == SerialBackupTab.EXPORT) {
                    ExportTabContent(
                        uiState = uiState,
                        isExporting = isExporting,
                        lastExportedSerial = lastExportedSerial,
                        lastExportedBundle = lastExportedBundle,
                        exportHistory = exportHistory,
                        onExportClick = {
                            isExporting = true
                            viewModel.exportDataToSerial { success, resultSerial, bundle ->
                                isExporting = false
                                if (success) {
                                    lastExportedSerial = resultSerial
                                    lastExportedBundle = bundle
                                    exportHistory = viewModel.getExportHistory()
                                    // Copy serial number automatically for user convenience
                                    val clip = ClipData.newPlainText("Montra Serial Number", resultSerial)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Serial number generated & copied to clipboard!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Export error: $resultSerial", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onCopySerial = { serial ->
                            val clip = ClipData.newPlainText("Montra Serial Number", serial)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(context, "Serial number copied!", Toast.LENGTH_SHORT).show()
                        },
                        onShareSerial = { serial, count ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Here is my Montra financial data backup serial number:\n$serial\n\nPaste this in Montra -> Settings -> Import Data to add all $count transaction(s) instantly."
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Backup Serial"))
                        },
                        onTestImport = { testSerial ->
                            performFetchForSerial(testSerial)
                        }
                    )
                } else {
                    ImportTabContent(
                        uiState = uiState,
                        serialInput = serialInput,
                        onSerialInputChange = {
                            serialInput = it
                            importErrorMessage = null
                            importSuccessMessage = null
                        },
                        isFetching = isFetching,
                        isImporting = isImporting,
                        previewBundle = previewBundle,
                        errorMessage = importErrorMessage,
                        successMessage = importSuccessMessage,
                        exportHistory = exportHistory,
                        onPasteClipboard = {
                            val clip = clipboardManager.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val pasted = clip.getItemAt(0).text?.toString()?.trim() ?: ""
                                if (pasted.isNotEmpty()) {
                                    performFetchForSerial(pasted)
                                }
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFetchClick = {
                            if (serialInput.isBlank()) {
                                importErrorMessage = "Please paste or enter a serial number"
                                return@ImportTabContent
                            }
                            performFetchForSerial(serialInput)
                        },
                        onSelectHistorySerial = { histSerial ->
                            performFetchForSerial(histSerial)
                        },
                        onImportClick = { bundle ->
                            isImporting = true
                            importErrorMessage = null
                            viewModel.importDataFromSerial(bundle) { success, message, _ ->
                                isImporting = false
                                if (success) {
                                    importSuccessMessage = message
                                    previewBundle = null
                                    serialInput = ""
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                } else {
                                    importErrorMessage = message
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportTabContent(
    uiState: ExpenseUiState,
    isExporting: Boolean,
    lastExportedSerial: String?,
    lastExportedBundle: SerialBackupBundle?,
    exportHistory: List<ExportHistoryItem>,
    onExportClick: () -> Unit,
    onCopySerial: (String) -> Unit,
    onShareSerial: (String, Int) -> Unit,
    onTestImport: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Current Data Overview Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MontraSurface,
                border = BorderStroke(1.dp, MontraBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Current Account Data",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MontraTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Ready to Export",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DataStatPill(
                            label = "Total Transactions",
                            value = "${uiState.allExpensesUnfiltered.size}",
                            icon = Icons.Filled.ReceiptLong
                        )
                        DataStatPill(
                            label = "Active Budgets",
                            value = "${uiState.budgetStatuses.count { it.monthlyLimit > 0 }}",
                            icon = Icons.Filled.AccountBalanceWallet
                        )
                        DataStatPill(
                            label = "Net Balance",
                            value = FormatUtils.formatCurrency(uiState.totalBalance, uiState.selectedCurrency),
                            icon = Icons.Filled.AttachMoney
                        )
                    }
                }
            }
        }

        // Export Action Button
        item {
            Button(
                onClick = onExportClick,
                enabled = !isExporting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_generate_serial_export")
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Generating Serial & Uploading...", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(
                        imageVector = Icons.Filled.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Random Serial Number",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Newly Generated Serial Card
        if (lastExportedSerial != null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF1E1B4B),
                    border = BorderStroke(1.5.dp, Color(0xFF818CF8))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Serial Generated Successfully!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4ADE80)
                                )
                            }
                            Text(
                                text = "Auto-copied",
                                fontSize = 11.sp,
                                color = Color(0xFFC7D2FE)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Serial Key display
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF4338CA)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCopySerial(lastExportedSerial) }
                        ) {
                            Text(
                                text = lastExportedSerial,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Save this serial number. Anyone who enters or pastes this serial number in the Import Data tab will receive all ${lastExportedBundle?.expenses?.size ?: uiState.allExpensesUnfiltered.size} items.",
                            fontSize = 12.sp,
                            color = Color(0xFFC7D2FE),
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onCopySerial(lastExportedSerial) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_copy_serial"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF818CF8)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Serial", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    onShareSerial(
                                        lastExportedSerial,
                                        lastExportedBundle?.expenses?.size ?: uiState.allExpensesUnfiltered.size
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_share_serial"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F46E5),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onTestImport(lastExportedSerial) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_test_import_serial"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Switch to Import Tab & Test", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Previous Exported Serial Numbers
        if (exportHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Previously Generated Serial Numbers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MontraTextPrimary
                )
            }

            items(exportHistory) { item ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MontraSurface,
                    border = BorderStroke(1.dp, MontraBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.serialKey,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MontraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.itemCount} items • ${item.formattedDate}",
                                fontSize = 12.sp,
                                color = MontraTextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { onTestImport(item.serialKey) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "Test import",
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onCopySerial(item.serialKey) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MontraSurfaceElevated)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MontraTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ImportTabContent(
    uiState: ExpenseUiState,
    serialInput: String,
    onSerialInputChange: (String) -> Unit,
    isFetching: Boolean,
    isImporting: Boolean,
    previewBundle: SerialBackupBundle?,
    errorMessage: String?,
    successMessage: String?,
    exportHistory: List<ExportHistoryItem> = emptyList(),
    onPasteClipboard: () -> Unit,
    onFetchClick: () -> Unit,
    onSelectHistorySerial: (String) -> Unit = {},
    onImportClick: (SerialBackupBundle) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Enter or Paste Serial Number",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MontraTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Paste any random serial number generated from another device to import and add all of its data into your account.",
                fontSize = 12.sp,
                color = MontraTextSecondary,
                lineHeight = 17.sp
            )
        }

        // Input Field + Paste Button
        item {
            Column {
                OutlinedTextField(
                    value = serialInput,
                    onValueChange = onSerialInputChange,
                    placeholder = {
                        Text(
                            "e.g. MNTR-8K4F-29AP-W7ZQ",
                            color = MontraTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_serial_number"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MontraSurface,
                        unfocusedContainerColor = MontraSurface,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = MontraBorder,
                        focusedTextColor = MontraTextPrimary,
                        unfocusedTextColor = MontraTextPrimary
                    ),
                    trailingIcon = {
                        if (serialInput.isNotEmpty()) {
                            IconButton(onClick = { onSerialInputChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MontraTextSecondary)
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onPasteClipboard,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_paste_clipboard"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MontraBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MontraSurface,
                            contentColor = MontraTextPrimary
                        )
                    ) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paste from Clipboard", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onFetchClick,
                        enabled = !isFetching && serialInput.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_fetch_serial_data"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        )
                    ) {
                        if (isFetching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Find Data", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Quick Pick from recent serials on this device
        if (exportHistory.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Or pick from recent serials on this device:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(exportHistory.take(5)) { hist ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (serialInput == hist.serialKey) Color(0xFF312E81) else MontraSurface,
                                border = BorderStroke(1.dp, if (serialInput == hist.serialKey) Color(0xFF818CF8) else MontraBorder),
                                modifier = Modifier.clickable {
                                    onSelectHistorySerial(hist.serialKey)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Key,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = hist.serialKey,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MontraTextPrimary
                                    )
                                    Text(
                                        text = "(${hist.itemCount} items)",
                                        fontSize = 11.sp,
                                        color = MontraTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error message banner
        if (errorMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7F1D1D).copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFF87171))
                        Text(
                            text = errorMessage,
                            fontSize = 13.sp,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }
        }

        // Success message banner
        if (successMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF064E3B).copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF34D399))
                        Text(
                            text = successMessage,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                }
            }
        }

        // Preview of Fetched Data
        if (previewBundle != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MontraSurface,
                    border = BorderStroke(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Data Found in Serial",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF818CF8)
                                )
                                Text(
                                    text = previewBundle.serialKey,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MontraTextPrimary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Ready to Add",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DataStatPill(
                                label = "Transactions",
                                value = "${previewBundle.expenses.size}",
                                icon = Icons.Filled.ReceiptLong
                            )
                            DataStatPill(
                                label = "Income / Exp",
                                value = "+${previewBundle.incomeCount} / -${previewBundle.expenseCount}",
                                icon = Icons.Filled.SwapVert
                            )
                            DataStatPill(
                                label = "Budgets",
                                value = "${previewBundle.budgets.size}",
                                icon = Icons.Filled.AccountBalanceWallet
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bundle metadata
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MontraSurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Author: ${previewBundle.authorName}",
                                fontSize = 12.sp,
                                color = MontraTextSecondary
                            )
                            Text(
                                text = "Exported: ${previewBundle.formattedDate}",
                                fontSize = 12.sp,
                                color = MontraTextSecondary
                            )
                            if (previewBundle.customCategories.isNotEmpty()) {
                                Text(
                                    text = "Custom Categories: ${previewBundle.customCategories.joinToString { it.name }}",
                                    fontSize = 12.sp,
                                    color = MontraTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Add All Data Button
                        Button(
                            onClick = { onImportClick(previewBundle) },
                            enabled = !isImporting,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_confirm_add_serial_data")
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Adding Data to Account...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add All Data (${previewBundle.expenses.size} Items) to App",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DataStatPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MontraSurfaceElevated,
        modifier = Modifier.widthIn(min = 90.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MontraTextPrimary
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MontraTextSecondary,
                maxLines = 1
            )
        }
    }
}
