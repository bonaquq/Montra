package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImageCropUtils {
    private const val TAG = "ImageCropUtils"

    /**
     * Loads a Bitmap from a content or file Uri with memory safety and EXIF orientation correction.
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? {
        return try {
            val contentResolver = context.contentResolver

            // 1. Decode bounds first to calculate inSampleSize
            var input: InputStream? = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            input?.close()

            var sampleSize = 1
            var width = options.outWidth
            var height = options.outHeight

            while (width / 2 >= maxDimension || height / 2 >= maxDimension) {
                width /= 2
                height /= 2
                sampleSize *= 2
            }

            // 2. Decode actual scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            input = contentResolver.openInputStream(uri)
            val decodedBitmap = BitmapFactory.decodeStream(input, null, decodeOptions)
            input?.close()

            if (decodedBitmap == null) return null

            // 3. Correct EXIF orientation if needed
            val orientation = try {
                val exifInput = contentResolver.openInputStream(uri)
                if (exifInput != null) {
                    val exif = ExifInterface(exifInput)
                    val orient = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    exifInput.close()
                    orient
                } else ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                val rotated = Bitmap.createBitmap(
                    decodedBitmap, 0, 0,
                    decodedBitmap.width, decodedBitmap.height,
                    matrix, true
                )
                if (rotated != decodedBitmap) {
                    decodedBitmap.recycle()
                }
                rotated
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from uri $uri", e)
            null
        }
    }

    /**
     * Crops and renders the visible viewport area of an image into a clean square/circle bitmap,
     * and saves it to internal app storage.
     */
    fun cropAndSaveBitmap(
        context: Context,
        sourceBitmap: Bitmap,
        cropScale: Float,
        offsetX: Float,
        offsetY: Float,
        rotationDegrees: Float,
        viewportWidth: Float,
        viewportHeight: Float,
        cropDiameter: Float,
        outputDimension: Int = 512
    ): String? {
        return try {
            // Output bitmap
            val output = Bitmap.createBitmap(outputDimension, outputDimension, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            // Calculate base scale to fit source into viewport
            val baseScale = min(
                viewportWidth / sourceBitmap.width.toFloat(),
                viewportHeight / sourceBitmap.height.toFloat()
            )
            val effectiveScale = baseScale * cropScale

            // Transform matrix for source image to destination canvas
            val matrix = Matrix()

            // 1. Center source bitmap at origin
            matrix.postTranslate(-sourceBitmap.width / 2f, -sourceBitmap.height / 2f)

            // 2. Apply scale
            matrix.postScale(effectiveScale, effectiveScale)

            // 3. Apply user rotation
            matrix.postRotate(rotationDegrees)

            // 4. Translate by user pan offset on screen
            matrix.postTranslate(offsetX, offsetY)

            // 5. Scale to output dimension
            val scaleToOutput = outputDimension / max(1f, cropDiameter)
            matrix.postScale(scaleToOutput, scaleToOutput)

            // 6. Center on output canvas
            matrix.postTranslate(outputDimension / 2f, outputDimension / 2f)

            // Draw bitmap
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(sourceBitmap, matrix, paint)

            // Save to app internal storage
            val dir = File(context.filesDir, "profile_pictures")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = "pfp_${System.currentTimeMillis()}.jpg"
            val file = File(dir, fileName)

            val fos = FileOutputStream(file)
            output.compress(Bitmap.CompressFormat.JPEG, 92, fos)
            fos.flush()
            fos.close()
            output.recycle()

            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error cropping and saving bitmap", e)
            null
        }
    }
}
