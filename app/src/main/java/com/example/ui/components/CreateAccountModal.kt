package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SupportedCurrency
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary

@Composable
fun CreateAccountModal(
    onDismiss: () -> Unit,
    onCreateAccount: (name: String, email: String, pin: String, initialBalance: Double, currency: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("0.00") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MontraSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = MontraTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Create Account",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontraTextPrimary
                            )
                            Text(
                                text = "Set up your Montra profile",
                                fontSize = 13.sp,
                                color = MontraTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_create_account")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MontraTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error message banner
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF3B1E22))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFF87171),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Full Name Input
                Text(
                    text = "Full Name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MontraTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    placeholder = { Text("e.g. Alex Morgan", color = MontraTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MontraTextMuted)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MontraSurfaceElevated,
                        unfocusedContainerColor = MontraSurfaceElevated,
                        focusedBorderColor = MontraTextPrimary,
                        unfocusedBorderColor = MontraBorder,
                        focusedTextColor = MontraTextPrimary,
                        unfocusedTextColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_name")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Email Address Input
                Text(
                    text = "Email Address",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MontraTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    placeholder = { Text("e.g. alex.morgan@montra.app", color = MontraTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = MontraTextMuted)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MontraSurfaceElevated,
                        unfocusedContainerColor = MontraSurfaceElevated,
                        focusedBorderColor = MontraTextPrimary,
                        unfocusedBorderColor = MontraBorder,
                        focusedTextColor = MontraTextPrimary,
                        unfocusedTextColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_email")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Security PIN / Password
                Text(
                    text = "Security PIN / Password",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MontraTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it; errorMessage = null },
                    placeholder = { Text("4-digit PIN or password", color = MontraTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MontraTextMuted)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MontraSurfaceElevated,
                        unfocusedContainerColor = MontraSurfaceElevated,
                        focusedBorderColor = MontraTextPrimary,
                        unfocusedBorderColor = MontraBorder,
                        focusedTextColor = MontraTextPrimary,
                        unfocusedTextColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_pin")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Starting Balance & Currency
                Text(
                    text = "Starting Balance",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MontraTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it; errorMessage = null },
                    placeholder = { Text("0.00", color = MontraTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Filled.AttachMoney, contentDescription = null, tint = MontraTextMuted)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MontraSurfaceElevated,
                        unfocusedContainerColor = MontraSurfaceElevated,
                        focusedBorderColor = MontraTextPrimary,
                        unfocusedBorderColor = MontraBorder,
                        focusedTextColor = MontraTextPrimary,
                        unfocusedTextColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_balance")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Currency selector pills
                Text(
                    text = "Preferred Currency",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MontraTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currencies = SupportedCurrency.entries.map { it.code }
                    items(currencies) { cur ->
                        val isSelected = selectedCurrency == cur
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MontraButtonBg else MontraSurfaceElevated)
                                .clickable { selectedCurrency = cur }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cur,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MontraTextPrimary else MontraTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Please enter your name"
                            return@Button
                        }
                        if (email.isBlank() || !email.contains("@")) {
                            errorMessage = "Please enter a valid email address"
                            return@Button
                        }
                        val balance = balanceText.toDoubleOrNull() ?: 0.0
                        onCreateAccount(name.trim(), email.trim(), pin.trim().ifEmpty { "1234" }, balance, selectedCurrency)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MontraButtonBg,
                        contentColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("create_account_submit_btn")
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_cancel_create_account")
                ) {
                    Text(
                        text = "Cancel",
                        color = MontraTextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
