package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SupportedCurrency
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import com.example.util.AmountInputUtils

@Composable
fun AuthScreen(
    onSignIn: (email: String, pass: String) -> Unit,
    onSignUp: (email: String, pass: String, name: String, initialBalance: Double, currency: SupportedCurrency) -> Unit,
    onContinueAsGuest: () -> Unit,
    onSignInWithGoogle: (() -> Unit)? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("0") }
    var selectedCurrency by remember { mutableStateOf(SupportedCurrency.USD) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val displayError = localError ?: errorMessage

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MontraBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Brand Icon & Title
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MontraButtonBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBalanceWallet,
                contentDescription = null,
                tint = MontraTextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Montra",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MontraTextPrimary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF2563EB))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "beta",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSignUpMode) "Create an account to track your wealth" else "Welcome back! Sign in to continue",
            fontSize = 14.sp,
            color = MontraTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Toggle Pill: Log In | Sign Up
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MontraSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isSignUpMode) MontraButtonBg else MontraSurface)
                    .clickable {
                        isSignUpMode = false
                        localError = null
                    }
                    .padding(vertical = 10.dp)
                    .testTag("tab_auth_login"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log In",
                    fontSize = 14.sp,
                    fontWeight = if (!isSignUpMode) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (!isSignUpMode) MontraTextPrimary else MontraTextMuted
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSignUpMode) MontraButtonBg else MontraSurface)
                    .clickable {
                        isSignUpMode = true
                        localError = null
                    }
                    .padding(vertical = 10.dp)
                    .testTag("tab_auth_signup"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = if (isSignUpMode) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSignUpMode) MontraTextPrimary else MontraTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error Banner if present
        if (displayError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = displayError,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Form Fields
        if (isSignUpMode) {
            // Name Field
            MontraAuthInputField(
                label = "Full Name",
                value = name,
                onValueChange = { name = it },
                icon = Icons.Filled.Person,
                placeholder = "John Doe",
                testTag = "input_auth_name"
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Email Field
        MontraAuthInputField(
            label = "Email Address",
            value = email,
            onValueChange = { email = it },
            icon = Icons.Filled.Email,
            placeholder = "name@example.com",
            keyboardType = KeyboardType.Email,
            testTag = "input_auth_email"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password Field
        MontraAuthInputField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            icon = Icons.Filled.Lock,
            placeholder = "••••••••",
            isPassword = true,
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
            keyboardType = KeyboardType.Password,
            testTag = "input_auth_password"
        )

        if (isSignUpMode) {
            Spacer(modifier = Modifier.height(14.dp))

            // Initial Balance Field
            MontraAuthInputField(
                label = "Initial Balance",
                value = initialBalanceText,
                onValueChange = { initialBalanceText = AmountInputUtils.sanitizeAmount(it) },
                icon = Icons.Filled.AccountBalanceWallet,
                placeholder = "1000",
                keyboardType = KeyboardType.Decimal,
                testTag = "input_auth_balance"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Preferred Currency Row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Preferred Currency",
                    fontSize = 12.sp,
                    color = MontraTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickCurrencies = listOf(SupportedCurrency.USD, SupportedCurrency.EUR, SupportedCurrency.MVR, SupportedCurrency.GBP)
                    quickCurrencies.forEach { cur ->
                        val isCurSelected = selectedCurrency == cur
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurSelected) MontraButtonBg else MontraSurface)
                                .border(1.dp, if (isCurSelected) MontraButtonBg else MontraBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedCurrency = cur }
                            .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cur.code,
                                fontSize = 12.sp,
                                fontWeight = if (isCurSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurSelected) MontraTextPrimary else MontraTextMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Google Sign-In Button
        if (onSignInWithGoogle != null) {
            Button(
                onClick = onSignInWithGoogle,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_auth_google")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleIconBadge(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider "or continue with email"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(MontraBorder))
                Text(
                    text = "  or continue with email  ",
                    fontSize = 12.sp,
                    color = MontraTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(MontraBorder))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Submit Button
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    localError = "Please enter both email and password"
                    return@Button
                }
                if (password.length < 4) {
                    localError = "Password should be at least 4 characters"
                    return@Button
                }
                localError = null
                if (isSignUpMode) {
                    val bal = initialBalanceText.toDoubleOrNull() ?: 0.0
                    onSignUp(email.trim(), password, name.trim(), bal, selectedCurrency)
                } else {
                    onSignIn(email.trim(), password)
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_auth_submit")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isSignUpMode) "Create Account" else "Log In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Guest Account Button
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = onContinueAsGuest,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MontraTextSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_auth_guest")
        ) {
            Text(
                text = "Continue as Guest / Offline Mode",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun GoogleIconBadge(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w / 2f

        // Draw clean multi-colored Google 'G' ring arcs
        val strokeWidth = w * 0.22f
        val arcSize = androidx.compose.ui.geometry.Size(w - strokeWidth, h - strokeWidth)
        val arcTopLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f)

        // Red top-left
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Blue right
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 315f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Green bottom
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Yellow bottom-left
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Center blue bar
        drawLine(
            color = Color(0xFF4285F4),
            start = androidx.compose.ui.geometry.Offset(cx - 1f, cy),
            end = androidx.compose.ui.geometry.Offset(w - strokeWidth / 3f, cy),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun MontraAuthInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MontraTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MontraSurface)
                .border(1.dp, MontraBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MontraTextMuted,
                    modifier = Modifier.size(18.dp)
                )

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MontraTextPrimary
                    ),
                    cursorBrush = SolidColor(MontraTextPrimary),
                    singleLine = true,
                    visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(testTag),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = MontraTextMuted
                            )
                        }
                        innerTextField()
                    }
                )

                if (isPassword && onTogglePasswordVisibility != null) {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle password",
                            tint = MontraTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
