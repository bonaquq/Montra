package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.BiometricAuthManager
import com.example.auth.BiometricStatus
import com.example.auth.findFragmentActivity
import com.example.ui.ExpenseUiState
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary

@Composable
fun BiometricLockScreen(
    uiState: ExpenseUiState,
    onUnlockSuccess: () -> Unit,
    onVerifyPin: (String, (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPinMode by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var isVerifyingPin by remember { mutableStateOf(false) }
    val biometricStatus = remember { BiometricAuthManager.getBiometricStatus(context) }

    fun triggerBiometricPrompt() {
        errorMessage = null
        if (activity == null) {
            errorMessage = "FragmentActivity not available"
            return
        }

        BiometricAuthManager.showBiometricPrompt(
            activity = activity,
            title = "Unlock Montra",
            subtitle = "Confirm your fingerprint or face recognition",
            description = "Access your financial dashboard and transactions",
            negativeButtonText = "Enter PIN / Password",
            allowDeviceCredential = false,
            onSuccess = {
                errorMessage = null
                onUnlockSuccess()
            },
            onError = { code, msg ->
                // BiometricPrompt.ERROR_USER_CANCELED = 10, ERROR_NEGATIVE_BUTTON = 13
                if (code == 13) {
                    // User opted for PIN / Password
                    isPinMode = true
                } else if (code != 10) {
                    errorMessage = msg
                }
            },
            onFailed = {
                errorMessage = "Biometric not recognized. Please try again or use PIN."
            }
        )
    }

    // Auto-prompt on launch if biometrics are enrolled
    LaunchedEffect(Unit) {
        if (biometricStatus == BiometricStatus.Available) {
            triggerBiometricPrompt()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_biometric_lock"),
        color = MontraBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: App Branding & Shield
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                        .border(2.dp, Color(0xFF2563EB).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPinMode) Icons.Filled.Key else Icons.Filled.Fingerprint,
                            contentDescription = "Security Lock",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isPinMode) "Enter PIN or Password" else "Montra Secured",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MontraTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isPinMode)
                        "Enter your account PIN or password to unlock"
                    else
                        "Scan your fingerprint or face recognition to access your expenses",
                    fontSize = 14.sp,
                    color = MontraTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Active Account Badge
                val accountName = uiState.activeAccount?.name ?: uiState.authUser?.displayName ?: "User Account"
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MontraSurface)
                        .border(1.dp, MontraBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = accountName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MontraTextPrimary
                    )
                }
            }

            // Middle Section: Error Banner & Hardware Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!isPinMode && biometricStatus != BiometricStatus.Available) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MontraSurface)
                            .border(1.dp, MontraBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Note: ${biometricStatus.message}. You can use your PIN/Password or test prompt.",
                            color = MontraTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // PIN / Passcode Entry Area
                AnimatedVisibility(
                    visible = isPinMode,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // PIN Indicator Circles
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            for (i in 0 until 4) {
                                val filled = i < enteredPin.length
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) Color(0xFF2563EB) else MontraSurfaceElevated)
                                        .border(
                                            1.5.dp,
                                            if (filled) Color(0xFF2563EB) else MontraBorder,
                                            CircleShape
                                        )
                                )
                            }
                        }

                        // Numeric Keypad
                        val keypad = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("C", "0", "DEL")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            keypad.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    row.forEach { key ->
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (key == "C" || key == "DEL") MontraSurface else MontraSurfaceElevated
                                                )
                                                .border(1.dp, MontraBorder, CircleShape)
                                                .clickable {
                                                    when (key) {
                                                        "C" -> {
                                                            enteredPin = ""
                                                            errorMessage = null
                                                        }
                                                        "DEL" -> {
                                                            if (enteredPin.isNotEmpty()) {
                                                                enteredPin = enteredPin.dropLast(1)
                                                                errorMessage = null
                                                            }
                                                        }
                                                        else -> {
                                                            if (enteredPin.length < 8) {
                                                                val newPin = enteredPin + key
                                                                enteredPin = newPin
                                                                errorMessage = null
                                                                if (newPin.length >= 4) {
                                                                    isVerifyingPin = true
                                                                    onVerifyPin(newPin) { valid ->
                                                                        isVerifyingPin = false
                                                                        if (valid) {
                                                                            onUnlockSuccess()
                                                                        } else if (newPin.length >= 6) {
                                                                            errorMessage = "Incorrect PIN. Try again."
                                                                            enteredPin = ""
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                .testTag("keypad_$key"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (key == "DEL") {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                    contentDescription = "Backspace",
                                                    tint = MontraTextSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = key,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MontraTextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Enter your 4-digit security PIN",
                            fontSize = 12.sp,
                            color = MontraTextMuted
                        )
                    }
                }
            }

            // Bottom Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isPinMode) {
                    // Primary: Prompt Biometric
                    Button(
                        onClick = { triggerBiometricPrompt() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_trigger_biometric")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Fingerprint / Face",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Secondary: Switch to PIN mode
                    OutlinedButton(
                        onClick = { isPinMode = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MontraTextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_switch_to_pin")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Key,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MontraTextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Use PIN or Password",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // In PIN Mode: Option to return to Biometric
                    OutlinedButton(
                        onClick = {
                            isPinMode = false
                            enteredPin = ""
                            errorMessage = null
                            triggerBiometricPrompt()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MontraTextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_return_to_biometric")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Switch to Biometric Scan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
