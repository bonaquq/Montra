package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.UserAccount
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraIncomeGreen
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import java.io.File

@Composable
fun AccountManageModal(
    activeAccount: UserAccount?,
    allAccounts: List<UserAccount>,
    onDismiss: () -> Unit,
    onOpenCreateAccount: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onLogout: () -> Unit,
    onUpdateProfilePicture: (String?) -> Unit = {},
    onUpdateAccountProfile: (name: String, email: String, pfp: String?) -> Unit = { _, _, _ -> },
    onOpenDeveloperOptions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // State for crop modal
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // State for Camera Capture Temp File
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Activity Result Launchers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            pendingCropUri = tempCameraUri
        }
    }

    // Interactive Crop Modal Dialog
    pendingCropUri?.let { uri ->
        ProfilePictureCropModal(
            imageUri = uri,
            onDismiss = { pendingCropUri = null },
            onCropCompleted = { croppedPath ->
                pendingCropUri = null
                onUpdateProfilePicture(croppedPath)
                Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Photo Options Modal (Choose Gallery / Take Camera / Crop / Remove)
    if (showPhotoOptionsDialog) {
        Dialog(onDismissRequest = { showPhotoOptionsDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(20.dp)),
                color = MontraSurface,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profile Photo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                        IconButton(onClick = { showPhotoOptionsDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = MontraTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Choose from Gallery / Photo Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MontraSurfaceElevated)
                            .clickable {
                                showPhotoOptionsDialog = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(14.dp)
                            .testTag("btn_pick_photo_gallery"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text("Choose from Gallery", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MontraTextPrimary)
                            Text("Pick and crop an image", fontSize = 12.sp, color = MontraTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Take with Camera
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MontraSurfaceElevated)
                            .clickable {
                                showPhotoOptionsDialog = false
                                try {
                                    val tempFile = File(context.cacheDir, "camera_pfp_${System.currentTimeMillis()}.jpg")
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        tempFile
                                    )
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    // Fallback to gallery picker
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                            .padding(14.dp)
                            .testTag("btn_take_photo_camera"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF059669).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text("Take Photo", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MontraTextPrimary)
                            Text("Capture with camera & crop", fontSize = 12.sp, color = MontraTextSecondary)
                        }
                    }

                    // 3. Remove Photo (if active account has a custom photo)
                    if (!activeAccount?.profilePictureUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MontraSurfaceElevated)
                                .clickable {
                                    showPhotoOptionsDialog = false
                                    onUpdateProfilePicture(null)
                                    Toast.makeText(context, "Profile picture removed", Toast.LENGTH_SHORT).show()
                                }
                                .padding(14.dp)
                                .testTag("btn_remove_pfp"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text("Remove Picture", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF87171))
                                Text("Revert to initial monogram", fontSize = 12.sp, color = MontraTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Modal (Name & Email)
    if (showEditProfileDialog && activeAccount != null) {
        var editName by remember { mutableStateOf(activeAccount.name) }
        var editEmail by remember { mutableStateOf(activeAccount.email) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp)),
                color = MontraSurface,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Edit Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MontraIncomeGreen,
                            unfocusedBorderColor = MontraBorder,
                            focusedTextColor = MontraTextPrimary,
                            unfocusedTextColor = MontraTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MontraIncomeGreen,
                            unfocusedBorderColor = MontraBorder,
                            focusedTextColor = MontraTextPrimary,
                            unfocusedTextColor = MontraTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = MontraTextSecondary)
                        }

                        Button(
                            onClick = {
                                if (editName.isNotBlank()) {
                                    onUpdateAccountProfile(editName, editEmail, activeAccount.profilePictureUri)
                                    showEditProfileDialog = false
                                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    // Main Account Manage Modal
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
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontraTextPrimary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_account_manage")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MontraTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Account Profile Card with Avatar & Change Photo Badge
                if (activeAccount != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MontraSurfaceElevated)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar with Camera / Crop overlay badge
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable { showPhotoOptionsDialog = true }
                                    .testTag("btn_avatar_photo_options")
                            ) {
                                UserAvatar(
                                    account = activeAccount,
                                    size = 60.dp,
                                    fontSize = 20.sp,
                                    borderColor = Color(0xFF3F3F46)
                                )

                                // Camera edit badge on avatar bottom-right
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color(0xFF059669))
                                        .border(1.5.dp, MontraSurfaceElevated, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Change profile picture",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = activeAccount.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontraTextPrimary
                                    )
                                    IconButton(
                                        onClick = { showEditProfileDialog = true },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Edit Profile",
                                            tint = MontraTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = activeAccount.email,
                                    fontSize = 13.sp,
                                    color = MontraTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Active Profile • ${activeAccount.currencyCode}",
                                        fontSize = 11.sp,
                                        color = MontraIncomeGreen
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MontraSurfaceElevated)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No Account Active",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MontraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Create an account to personalize your Montra experience",
                                fontSize = 13.sp,
                                color = MontraTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Saved Accounts list if more than 1
                if (allAccounts.size > 1) {
                    Text(
                        text = "Switch Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MontraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((allAccounts.size * 60).coerceAtMost(180).dp)
                    ) {
                        items(allAccounts) { account ->
                            val isCurrent = account.id == activeAccount?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) MontraSurfaceElevated else Color.Transparent)
                                    .clickable { onSwitchAccount(account.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    UserAvatar(
                                        account = account,
                                        size = 36.dp,
                                        fontSize = 12.sp
                                    )
                                    Column {
                                        Text(
                                            text = account.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MontraTextPrimary
                                        )
                                        Text(
                                            text = account.email,
                                            fontSize = 11.sp,
                                            color = MontraTextMuted
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Active",
                                        tint = MontraIncomeGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MontraBorder)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Add / Create Another Account Button
                Button(
                    onClick = {
                        onDismiss()
                        onOpenCreateAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MontraButtonBg,
                        contentColor = MontraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_create_another_account")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Another Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Developer Options Button
                OutlinedButton(
                    onClick = onOpenDeveloperOptions,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF34D399)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_account_developer_options")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Terminal,
                        contentDescription = "Developer Options",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Developer Options",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF34D399)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF064E3B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DEV CODE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Log Out Button
                if (activeAccount != null) {
                    OutlinedButton(
                        onClick = {
                            onLogout()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF87171)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_logout_account")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
