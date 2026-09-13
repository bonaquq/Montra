package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import com.example.auth.BiometricAuthManager
import com.example.auth.BiometricStatus
import com.example.auth.findFragmentActivity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ExpenseUiState
import com.example.ui.theme.MontraBackground
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraButtonBgActive
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary

@Composable
fun SettingsScreenContent(
    uiState: ExpenseUiState,
    onBack: () -> Unit,
    onOpenAccountManage: () -> Unit,
    onOpenCreateAccount: () -> Unit,
    onLogout: () -> Unit,
    onCurrencySelected: (com.example.data.SupportedCurrency) -> Unit = {},
    onSetTheme: (Boolean) -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onToggleBiometric: (Boolean) -> Unit = {},
    onLockAppNow: () -> Unit = {},
    onClearAllTransactions: () -> Unit = {},
    onOpenDeveloperOptions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val biometricStatus = remember { BiometricAuthManager.getBiometricStatus(context) }
    var biometricInfoDialogText by remember { mutableStateOf<String?>(null) }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var isCurrencyPickerOpen by remember { mutableStateOf(false) }
    var isThemePickerOpen by remember { mutableStateOf(false) }
    var isAboutDialogOpen by remember { mutableStateOf(false) }
    var isClearDataDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MontraBackground)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_settings_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MontraTextPrimary
                )
            }

            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MontraTextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // My Account Card (Clickable to manage profile / create account)
            item {
                val account = uiState.activeAccount
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MontraSurface)
                        .clickable(onClick = onOpenAccountManage)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .testTag("card_my_account"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MontraSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            if (account != null) {
                                Text(
                                    text = account.initials,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontraTextPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MontraTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = account?.name ?: "My Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MontraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = account?.email ?: "Manage your profile",
                                fontSize = 13.sp,
                                color = MontraTextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Manage Profile",
                        tint = MontraTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Settings Options List
            item {
                SettingsItemRow(
                    icon = Icons.Filled.AttachMoney,
                    title = "Currency",
                    value = "${uiState.selectedCurrency.name} (${uiState.selectedCurrency.symbol})",
                    onClick = { isCurrencyPickerOpen = true }
                )
                Spacer(modifier = Modifier.height(10.dp))

                SettingsItemRow(
                    icon = if (uiState.isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    title = "Theme",
                    value = if (uiState.isDarkMode) "Dark" else "Light",
                    onClick = { isThemePickerOpen = true },
                    modifier = Modifier.testTag("item_settings_theme")
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Notifications Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MontraSurface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MontraSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = MontraTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "Notifications",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MontraTextPrimary
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MontraButtonBg,
                            uncheckedThumbColor = MontraTextMuted,
                            uncheckedTrackColor = MontraSurfaceElevated
                        ),
                        modifier = Modifier.testTag("switch_notifications")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Biometric Security Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MontraSurface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MontraSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = "Biometric Lock",
                                tint = if (uiState.isBiometricEnabled) Color(0xFF3B82F6) else MontraTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Biometric Lock",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MontraTextPrimary
                            )
                            Text(
                                text = if (uiState.isBiometricEnabled)
                                    "Secured with fingerprint / face"
                                else
                                    "Fingerprint or face recognition",
                                fontSize = 12.sp,
                                color = MontraTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                val act = context.findFragmentActivity()
                                if (act != null && biometricStatus == BiometricStatus.Available) {
                                    BiometricAuthManager.showBiometricPrompt(
                                        activity = act,
                                        title = "Enable Biometric Lock",
                                        subtitle = "Confirm fingerprint or face recognition to activate",
                                        onSuccess = {
                                            onToggleBiometric(true)
                                        },
                                        onError = { code, msg ->
                                            if (code != 10 && code != 13) {
                                                onToggleBiometric(true)
                                            }
                                        }
                                    )
                                } else {
                                    onToggleBiometric(true)
                                }
                            } else {
                                onToggleBiometric(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB),
                            uncheckedThumbColor = MontraTextMuted,
                            uncheckedTrackColor = MontraSurfaceElevated
                        ),
                        modifier = Modifier.testTag("switch_biometric_lock")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.isBiometricEnabled) {
                    SettingsItemRow(
                        icon = Icons.Filled.Lock,
                        title = "Lock App Now",
                        value = "Require biometric to open",
                        onClick = onLockAppNow,
                        modifier = Modifier.testTag("item_settings_lock_now")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                SettingsItemRow(
                    icon = Icons.Filled.DeleteSweep,
                    title = "Clear All Transactions",
                    value = "${uiState.allExpensesUnfiltered.size} items",
                    onClick = { isClearDataDialogOpen = true },
                    modifier = Modifier.testTag("item_settings_clear_transactions")
                )
                Spacer(modifier = Modifier.height(10.dp))

                SettingsItemRow(
                    icon = Icons.Filled.Info,
                    title = "About",
                    value = null,
                    onClick = { isAboutDialogOpen = true },
                    modifier = Modifier.testTag("item_settings_about")
                )
                Spacer(modifier = Modifier.height(10.dp))

                SettingsItemRow(
                    icon = Icons.Filled.Terminal,
                    title = "Developer Options",
                    value = if (uiState.isDeveloperUnlocked) "Unlocked" else "Passcode Required",
                    onClick = onOpenDeveloperOptions,
                    modifier = Modifier.testTag("item_settings_developer_options")
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Buttons: Create Account & Log Out
            item {
                if (uiState.activeAccount == null) {
                    Button(
                        onClick = onOpenCreateAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MontraButtonBg,
                            contentColor = MontraTextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_settings_create_account")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Account",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (uiState.authUser != null || uiState.activeAccount != null) {
                            onLogout()
                        } else {
                            onOpenCreateAccount()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MontraSurface,
                        contentColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_settings_logout")
                ) {
                    Icon(
                        imageVector = if (uiState.authUser != null || uiState.activeAccount != null) Icons.AutoMirrored.Filled.Logout else Icons.Filled.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.authUser != null) "Log Out (${uiState.authUser.email ?: "Account"})" else if (uiState.activeAccount != null) "Log Out" else "Sign In / Create Account",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.authUser != null || uiState.activeAccount != null) Color(0xFFF87171) else MontraTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (isCurrencyPickerOpen) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { isCurrencyPickerOpen = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MontraSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Select Preferred Currency",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose your base currency for balances & transactions",
                        fontSize = 12.sp,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(com.example.data.SupportedCurrency.entries) { cur ->
                            val isSelected = cur == uiState.selectedCurrency
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MontraButtonBg else MontraSurfaceElevated)
                                    .clickable {
                                        onCurrencySelected(cur)
                                        isCurrencyPickerOpen = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = cur.code,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraTextPrimary
                                    )
                                    Text(
                                        text = cur.displayName,
                                        fontSize = 13.sp,
                                        color = MontraTextSecondary
                                    )
                                }
                                Text(
                                    text = cur.symbol,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) MontraTextPrimary else MontraTextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { isCurrencyPickerOpen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MontraSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = MontraTextPrimary)
                    }
                }
            }
        }
    }

    if (isThemePickerOpen) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { isThemePickerOpen = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MontraSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 16.dp)
                    .testTag("dialog_theme_picker")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Theme",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                        IconButton(
                            onClick = { isThemePickerOpen = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MontraTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Customize your visual style with Dark or Light mode",
                        fontSize = 12.sp,
                        color = MontraTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dark Theme Option
                    ThemeOptionCard(
                        title = "Dark Theme",
                        description = "Sleek charcoal night palette (Default)",
                        icon = Icons.Filled.DarkMode,
                        isSelected = uiState.isDarkMode,
                        onClick = {
                            onSetTheme(true)
                            isThemePickerOpen = false
                        },
                        testTag = "btn_theme_dark"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Light Theme Option
                    ThemeOptionCard(
                        title = "Light Theme",
                        description = "Crisp, bright high-contrast light palette",
                        icon = Icons.Filled.LightMode,
                        isSelected = !uiState.isDarkMode,
                        onClick = {
                            onSetTheme(false)
                            isThemePickerOpen = false
                        },
                        testTag = "btn_theme_light"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { isThemePickerOpen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MontraSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done", color = MontraTextPrimary)
                    }
                }
            }
        }
    }

    if (isAboutDialogOpen) {
        val uriHandler = LocalUriHandler.current
        val instagramAccounts = listOf(
            Pair("@hextechzy._", "https://www.instagram.com/hextechzy._/"),
            Pair("@a4ahnaf_prvt", "https://www.instagram.com/a4ahnaf_prvt")
        )

        androidx.compose.ui.window.Dialog(onDismissRequest = { isAboutDialogOpen = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MontraSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(vertical = 16.dp)
                    .testTag("dialog_about_montra")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "About Montra",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontraTextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2563EB))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("badge_about_beta")
                            ) {
                                Text(
                                    text = "beta",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = { isAboutDialogOpen = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MontraTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Description with "Beta" highlighted
                    val descriptionText = buildAnnotatedString {
                        append("This app is currently in ")
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold,
                                background = Color(0xFF2563EB).copy(alpha = 0.25f)
                            )
                        ) {
                            append("Beta")
                        }
                        append(". We are constantly improving and adding new features. Expect frequent updates and a few bugs as we work toward the official launch.")
                    }

                    Text(
                        text = descriptionText,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MontraTextSecondary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Instagram Redirect with Instagram Icon
                    Text(
                        text = "Follow Us on Instagram",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MontraTextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        instagramAccounts.forEachIndexed { index, (handle, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MontraSurfaceElevated)
                                    .border(BorderStroke(1.dp, MontraBorder), RoundedCornerShape(14.dp))
                                    .clickable {
                                        try {
                                            uriHandler.openUri(url)
                                        } catch (_: Exception) {
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .testTag(if (index == 0) "btn_instagram_redirect" else "btn_instagram_redirect_${handle.removePrefix("@")}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    InstagramIcon(
                                        modifier = Modifier.size(22.dp),
                                        tint = MontraTextPrimary
                                    )
                                    Text(
                                        text = handle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MontraTextPrimary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "Open Instagram $handle",
                                    tint = MontraTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { isAboutDialogOpen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MontraSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = MontraTextPrimary)
                    }
                }
            }
        }
    }

    if (isClearDataDialogOpen) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { isClearDataDialogOpen = false },
            title = {
                Text(
                    text = "Clear All Transactions",
                    fontWeight = FontWeight.Bold,
                    color = MontraTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete all transactions? This will leave your transaction history clean for fresh use and cannot be undone.",
                    color = MontraTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        isClearDataDialogOpen = false
                        onClearAllTransactions()
                    },
                    modifier = Modifier.testTag("btn_confirm_clear_transactions")
                ) {
                    Text("Clear All", color = androidx.compose.ui.graphics.Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { isClearDataDialogOpen = false }
                ) {
                    Text("Cancel", color = MontraTextSecondary)
                }
            },
            containerColor = MontraSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun InstagramIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val strokeW = sizePx * 0.1f
        val cornerRadius = CornerRadius(sizePx * 0.28f, sizePx * 0.28f)

        // Outer rounded box
        drawRoundRect(
            color = tint,
            topLeft = Offset(strokeW / 2f, strokeW / 2f),
            size = Size(sizePx - strokeW, sizePx - strokeW),
            cornerRadius = cornerRadius,
            style = Stroke(width = strokeW)
        )

        // Camera lens (center circle)
        drawCircle(
            color = tint,
            radius = sizePx * 0.22f,
            center = center,
            style = Stroke(width = strokeW)
        )

        // Flash dot (top-right)
        drawCircle(
            color = tint,
            radius = strokeW * 0.75f,
            center = Offset(sizePx * 0.74f, sizePx * 0.26f)
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MontraSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MontraSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MontraTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MontraTextPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = MontraTextSecondary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MontraTextMuted,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) MontraButtonBgActive else MontraSurfaceElevated)
            .border(
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) Color(0xFF2563EB) else MontraBorder
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.2f) else MontraSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF2563EB) else MontraTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MontraTextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MontraTextSecondary
                )
            }
        }

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (isSelected) Color(0xFF2563EB) else MontraTextMuted,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                )
            }
        }
    }
}

