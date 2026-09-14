package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.UserAccount
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextPrimary

@Composable
fun UserAvatar(
    account: UserAccount?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fontSize: TextUnit = 16.sp,
    borderColor: Color? = MontraBorder,
    backgroundColor: Color = MontraSurfaceElevated
) {
    val photoUri = account?.profilePictureUri

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape)
                else Modifier
            )
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = photoUri,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account?.initials ?: "MO",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account?.initials ?: "MO",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                    }
                }
            )
        } else if (account != null) {
            Text(
                text = account.initials,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = MontraTextPrimary
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User Avatar",
                tint = MontraTextPrimary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}
