package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MontraBorder
import com.example.ui.theme.MontraButtonBg
import com.example.ui.theme.MontraIncomeGreen
import com.example.ui.theme.MontraSurface
import com.example.ui.theme.MontraSurfaceElevated
import com.example.ui.theme.MontraTextMuted
import com.example.ui.theme.MontraTextPrimary
import com.example.ui.theme.MontraTextSecondary
import com.example.util.ImageCropUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

@Composable
fun ProfilePictureCropModal(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onCropCompleted: (savedFilePath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessingSave by remember { mutableStateOf(false) }

    // Transform State
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }

    // Load Bitmap in Background Coroutine
    LaunchedEffect(imageUri) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val bmp = ImageCropUtils.loadBitmapFromUri(context, imageUri)
            withContext(Dispatchers.Main) {
                loadedBitmap = bmp
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isProcessingSave) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MontraSurface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Crop Profile Photo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontraTextPrimary
                        )
                        Text(
                            text = "Pinch or drag to position your picture",
                            fontSize = 12.sp,
                            color = MontraTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isProcessingSave,
                        modifier = Modifier.testTag("btn_close_cropper")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MontraTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MontraSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MontraIncomeGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading image...",
                                fontSize = 13.sp,
                                color = MontraTextSecondary
                            )
                        }
                    }
                } else if (loadedBitmap != null) {
                    val bmp = loadedBitmap!!

                    // Interactive Viewport with circular crop mask
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0D0F14))
                            .border(1.dp, MontraBorder, RoundedCornerShape(18.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(0.8f, 4.0f)
                                    panOffsetX += pan.x
                                    panOffsetY += pan.y
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    panOffsetX += dragAmount.x
                                    panOffsetY += dragAmount.y
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val viewWidth = constraints.maxWidth.toFloat()
                        val viewHeight = constraints.maxHeight.toFloat()
                        val cropCircleDiameter = min(viewWidth, viewHeight) * 0.82f
                        val cropRadius = cropCircleDiameter / 2f
                        val centerOffset = Offset(viewWidth / 2f, viewHeight / 2f)

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // 1. Calculate image base scale
                            val baseScale = min(
                                viewWidth / bmp.width.toFloat(),
                                viewHeight / bmp.height.toFloat()
                            )
                            val effectiveScale = baseScale * zoomScale

                            // 2. Draw Transformed Image
                            translate(centerOffset.x + panOffsetX, centerOffset.y + panOffsetY) {
                                rotate(rotationDegrees) {
                                    scale(effectiveScale) {
                                        drawImage(
                                            image = bmp.asImageBitmap(),
                                            dstOffset = IntOffset(
                                                -bmp.width / 2,
                                                -bmp.height / 2
                                            ),
                                            dstSize = IntSize(bmp.width, bmp.height)
                                        )
                                    }
                                }
                            }

                            // 3. Draw Dark Semi-Transparent Mask Outside Crop Circle
                            val circlePath = Path().apply {
                                addOval(
                                    Rect(
                                        center = centerOffset,
                                        radius = cropRadius
                                    )
                                )
                            }

                            clipPath(circlePath, clipOp = ClipOp.Difference) {
                                drawRect(color = Color(0xCC000000))
                            }

                            // 4. Draw Guide Stroke around circle
                            drawCircle(
                                color = Color.White.copy(alpha = 0.85f),
                                radius = cropRadius,
                                center = centerOffset,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Controls Bar: Zoom Slider + Rotate + Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ZoomOut,
                            contentDescription = "Zoom Out",
                            tint = MontraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )

                        Slider(
                            value = zoomScale,
                            onValueChange = { zoomScale = it },
                            valueRange = 0.8f..3.5f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MontraIncomeGreen,
                                activeTrackColor = MontraIncomeGreen,
                                inactiveTrackColor = MontraSurfaceElevated
                            )
                        )

                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = "Zoom In",
                            tint = MontraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )

                        // Rotate 90°
                        IconButton(
                            onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MontraSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RotateRight,
                                contentDescription = "Rotate 90 degrees",
                                tint = MontraTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reset
                        IconButton(
                            onClick = {
                                zoomScale = 1.0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                                rotationDegrees = 0f
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MontraSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset Alignment",
                                tint = MontraTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Could not load selected image. Please try again.",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isProcessingSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MontraBorder)
                    ) {
                        Text("Cancel", color = MontraTextSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val bmp = loadedBitmap ?: return@Button
                            isProcessingSave = true
                            scope.launch {
                                val savedPath = withContext(Dispatchers.IO) {
                                    ImageCropUtils.cropAndSaveBitmap(
                                        context = context,
                                        sourceBitmap = bmp,
                                        cropScale = zoomScale,
                                        offsetX = panOffsetX,
                                        offsetY = panOffsetY,
                                        rotationDegrees = rotationDegrees,
                                        viewportWidth = 400f,
                                        viewportHeight = 400f,
                                        cropRadiusOrSize = 400f * 0.82f,
                                        outputDimension = 512
                                    )
                                }
                                isProcessingSave = false
                                if (savedPath != null) {
                                    onCropCompleted(savedPath)
                                } else {
                                    onDismiss()
                                }
                            }
                        },
                        enabled = loadedBitmap != null && !isProcessingSave,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("btn_save_crop_pfp"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        )
                    ) {
                        if (isProcessingSave) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Set Profile Photo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
