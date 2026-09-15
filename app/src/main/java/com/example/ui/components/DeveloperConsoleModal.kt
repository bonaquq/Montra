package com.example.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.material.icons.filled.PhotoLibrary
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.ExpenseUiState
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraIncomeGreen
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary

/**
 * Valid developer authorization access code.
 */
private val VALID_DEV_CODES = setOf(
    "0902"
)

/**
 * Passcode dialog to gate developer features.
 */
@Composable
fun DeveloperPasscodeDialog(
    onDismiss: () -> Unit,
    onCodeVerified: () -> Unit,
    modifier: Modifier = Modifier
) {
    var codeInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submitCode() {
        val trimmed = codeInput.trim().uppercase()
        if (trimmed in VALID_DEV_CODES) {
            errorMessage = null
            onCodeVerified()
        } else {
            errorMessage = "Invalid developer access code. Access denied."
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MontraSurface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Terminal,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Developer Access",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontraTextPrimary
                            )
                            Text(
                                text = "Authentication Required",
                                fontSize = 12.sp,
                                color = MontraTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_dev_passcode")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MontraTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Passcode Input Field
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = {
                        codeInput = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Special Access Code") },
                    placeholder = { Text("Enter secret passcode") },
                    singleLine = true,
                    isError = errorMessage != null,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitCode() }
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (errorMessage != null) Color(0xFFEF4444) else MontraTextMuted
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide Code" else "Show Code",
                                tint = MontraTextMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = MontraBorder,
                        errorBorderColor = Color(0xFFEF4444),
                        focusedTextColor = MontraTextPrimary,
                        unfocusedTextColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_developer_code")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = MontraTextSecondary)
                    }

                    Button(
                        onClick = { submitCode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_unlock_developer_console")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Unlock",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full Developer Console modal with system diagnostics, mock transaction injection,
 * receipt simulation, and database management tools.
 */
@Composable
fun DeveloperConsoleModal(
    uiState: ExpenseUiState,
    onDismiss: () -> Unit,
    onLockDeveloperMode: (() -> Unit)? = null,
    onInjectSampleData: () -> Unit,
    onInjectSingleTransaction: (title: String, amount: Double, category: String, isIncome: Boolean) -> Unit,
    onSimulateReceipt: () -> Unit,
    onClearAllTransactions: () -> Unit,
    onScanReceiptUri: ((Uri) -> Unit)? = null,
    onOpenAddExpenseWithScan: (() -> Unit)? = null,
    onOpenSerialTransfer: (() -> Unit)? = null,
    onResetAllBudgetsToZero: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isStateViewerExpanded by remember { mutableStateOf(false) }
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    var isScanSourceModalOpen by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null && onScanReceiptUri != null) {
                onScanReceiptUri(uri)
                Toast.makeText(context, "Scanning receipt image with OCR...", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                cameraTempUri?.let { uri ->
                    if (onScanReceiptUri != null) {
                        onScanReceiptUri(uri)
                        Toast.makeText(context, "Scanning camera capture with OCR...", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                try {
                    val tempFile = File(context.cacheDir, "dev_camera_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        tempFile
                    )
                    cameraTempUri = uri
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    android.util.Log.e("DeveloperConsole", "Failed to launch camera", e)
                }
            }
        }
    )

    fun launchCameraCapture() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                val tempFile = File(context.cacheDir, "dev_camera_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                cameraTempUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                android.util.Log.e("DeveloperConsole", "Failed to launch camera", e)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MontraSurface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Console Header
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF064E3B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeveloperMode,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Developer Console",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontraTextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF059669))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DEBUG",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "v1.0.0 • SQLite Room v4 • Authorized",
                                fontSize = 12.sp,
                                color = Color(0xFF34D399)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_dev_console")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MontraTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MontraBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Console Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section 1: System & DB Diagnostics
                    DevSectionCard(title = "System & Database Diagnostics", icon = Icons.Filled.BugReport) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DiagnosticRow("Runtime Target", "Android Jetpack Compose")
                            DiagnosticRow("Local DB Engine", "Room SQLite (v4, encrypted keys)")
                            DiagnosticRow("Active Profile ID", uiState.activeAccount?.id ?: "N/A (Guest)")
                            DiagnosticRow("Account Name", uiState.activeAccount?.name ?: "Personal Account")
                            DiagnosticRow("Account Email", uiState.activeAccount?.email ?: "user@montra.app")
                            DiagnosticRow("Transactions Count", "${uiState.allExpensesUnfiltered.size} entries")
                            DiagnosticRow("Net Balance", "${uiState.selectedCurrency.symbol}${String.format("%.2f", uiState.totalBalance)}")
                            DiagnosticRow("Total Spent", "${uiState.selectedCurrency.symbol}${String.format("%.2f", uiState.totalSpent)}")
                            DiagnosticRow("Total Income", "${uiState.selectedCurrency.symbol}${String.format("%.2f", uiState.totalIncome)}")
                            DiagnosticRow("Active Currency", "${uiState.selectedCurrency.name} (${uiState.selectedCurrency.code})")
                            DiagnosticRow("Biometric Lock", if (uiState.isBiometricEnabled) "Enabled" else "Disabled")
                        }
                    }

                    // Section 2: Mock Data & Sandbox Injections
                    DevSectionCard(title = "Sandbox Mock Generators", icon = Icons.Filled.ElectricBolt) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Quickly populate the app with realistic test transactions or inject specific amounts to verify charts, totals, and budget alerts.",
                                fontSize = 12.sp,
                                color = MontraTextSecondary
                            )

                            // Inject Test Dataset Button
                            Button(
                                onClick = {
                                    onInjectSampleData()
                                    Toast.makeText(context, "Injected 8 realistic test transactions!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E293B),
                                    contentColor = Color(0xFF38BDF8)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_dev_inject_sample_data")
                            ) {
                                Icon(Icons.Filled.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Inject 8 Test Transactions (Salary, Food, Rent)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add $5,000 Income
                                OutlinedButton(
                                    onClick = {
                                        onInjectSingleTransaction("Dev Test Payroll Bonus", 5000.0, "SALARY", true)
                                        Toast.makeText(context, "Added +$5,000.00 Income", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+ $5,000 Income", fontSize = 12.sp, color = MontraIncomeGreen, fontWeight = FontWeight.Bold)
                                }

                                // Add $150 Expense
                                OutlinedButton(
                                    onClick = {
                                        onInjectSingleTransaction("Dev Test Hardware Gear", 150.0, "SHOPPING", false)
                                        Toast.makeText(context, "Added -$150.00 Expense", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("- $150 Expense", fontSize = 12.sp, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Section 3: Smart Statement, Bill & Receipt OCR
                    DevSectionCard(title = "Smart Statement, Bill & Receipt Scan", icon = Icons.Filled.DocumentScanner) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "OCR extraction supports Bank Statements (Bank of Maldives, Maldives Islamic Bank), Utility Bills (STELCO, MWSC, Dhiraagu, Ooredoo, Medianet), and Store Receipts.",
                                fontSize = 12.sp,
                                color = MontraTextSecondary,
                                lineHeight = 17.sp
                            )

                            // Launch scanner to scan statement, bill or photo
                            Button(
                                onClick = {
                                    isScanSourceModalOpen = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF065F46),
                                    contentColor = Color(0xFF6EE7B7)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Document / Photo (Statement, Bill)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Open Add Expense with Smart Scan Active
                            if (onOpenAddExpenseWithScan != null) {
                                OutlinedButton(
                                    onClick = {
                                        onOpenAddExpenseWithScan()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Add Expense Screen (Smart Scan Unlocked)", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Section 3: Database Maintenance & Clean
                    DevSectionCard(title = "Database Wipe & Maintenance", icon = Icons.Filled.DeleteSweep) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Clear recorded transactions to test empty states and zero-balance displays.",
                                fontSize = 12.sp,
                                color = MontraTextSecondary
                            )

                            OutlinedButton(
                                onClick = {
                                    onClearAllTransactions()
                                    Toast.makeText(context, "Cleared all transaction records", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_dev_clear_all_data")
                            ) {
                                Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Wipe All Transactions (Reset to 0)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Section 4: Serial Transfer & Category Budgets
                    DevSectionCard(title = "Data Transfer & Category Budgets", icon = Icons.Filled.SwapHoriz) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Transfer encrypted ledger backups via serial number or reset category budget thresholds to 0.",
                                fontSize = 12.sp,
                                color = MontraTextSecondary
                            )

                            if (onOpenSerialTransfer != null) {
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onOpenSerialTransfer()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF6366F1),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Serial Backup & Transfer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (onResetAllBudgetsToZero != null) {
                                OutlinedButton(
                                    onClick = {
                                        onResetAllBudgetsToZero()
                                        Toast.makeText(context, "Reset all category budgets to 0", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reset All Category Budgets to 0", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Section 5: State Inspector & Diagnostics JSON
                    DevSectionCard(title = "Diagnostics Payload & State", icon = Icons.Filled.Code) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isStateViewerExpanded) "Hide Raw State" else "View State JSON",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.clickable { isStateViewerExpanded = !isStateViewerExpanded }
                                )

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val payload = buildDevDiagnosticsText(uiState)
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Montra Dev Diagnostics", payload))
                                        Toast.makeText(context, "Diagnostics copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Diagnostics",
                                        tint = MontraTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isStateViewerExpanded) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = buildDevDiagnosticsText(uiState),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    // Section 6: Lock Developer Mode Box
                    if (onLockDeveloperMode != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MontraSurfaceElevated)
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                                .testTag("box_lock_developer_mode")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Lock Developer Mode",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraTextPrimary
                                    )
                                }

                                Text(
                                    text = "Lock and exit Developer Mode. You will need to re-verify the developer passcode to access developer tools again.",
                                    fontSize = 12.sp,
                                    color = MontraTextSecondary,
                                    lineHeight = 16.sp
                                )

                                OutlinedButton(
                                    onClick = {
                                        onLockDeveloperMode()
                                        Toast.makeText(context, "Developer Mode Locked", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFEF4444)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("btn_lock_developer_mode")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Lock Developer Mode",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Session Action
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MontraButtonBg,
                        contentColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_dev_console_done")
                ) {
                    Text("Done", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (isScanSourceModalOpen) {
        Dialog(onDismissRequest = { isScanSourceModalOpen = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MontraSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DocumentScanner,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Scan Document / Statement",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontraTextPrimary
                            )
                        }
                        IconButton(onClick = { isScanSourceModalOpen = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = MontraTextMuted)
                        }
                    }

                    Text(
                        text = "Choose scan source:",
                        fontSize = 13.sp,
                        color = MontraTextSecondary
                    )

                    // Option 1: Take Photo with Camera
                    Surface(
                        onClick = {
                            isScanSourceModalOpen = false
                            launchCameraCapture()
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCamera,
                                    contentDescription = "Camera",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scan with Camera",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MontraTextPrimary
                                )
                                Text(
                                    text = "Take a photo of paper bill or statement",
                                    fontSize = 12.sp,
                                    color = MontraTextMuted
                                )
                            }
                        }
                    }

                    // Option 2: Choose from Photos
                    Surface(
                        onClick = {
                            isScanSourceModalOpen = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = MontraSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MontraBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = "Photos",
                                    tint = MontraTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Choose from Photos",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MontraTextPrimary
                                )
                                Text(
                                    text = "Select screenshot or image from gallery",
                                    fontSize = 12.sp,
                                    color = MontraTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DevSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MontraSurfaceElevated)
            .border(1.dp, MontraBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MontraTextPrimary
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = MontraBorder)
            content()
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MontraTextSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = MontraTextPrimary
        )
    }
}

private fun buildDevDiagnosticsText(state: ExpenseUiState): String {
    return """
{
  "app": "Montra Expense Tracker",
  "version": "1.0.0-debug",
  "account": {
    "id": "${state.activeAccount?.id ?: "guest"}",
    "name": "${state.activeAccount?.name ?: "Personal"}",
    "email": "${state.activeAccount?.email ?: "user@montra.app"}"
  },
  "metrics": {
    "totalTransactions": ${state.allExpensesUnfiltered.size},
    "currentBalance": ${state.totalBalance},
    "totalSpent": ${state.totalSpent},
    "totalIncome": ${state.totalIncome},
    "currency": "${state.selectedCurrency.code}"
  },
  "security": {
    "biometricEnabled": ${state.isBiometricEnabled},
    "darkMode": ${state.isDarkMode}
  }
}
    """.trimIndent()
}
