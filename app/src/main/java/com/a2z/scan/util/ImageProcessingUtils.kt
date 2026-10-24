package com.a2z.scan.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import com.a2z.scan.model.DocumentFilter
import java.io.InputStream
import kotlin.math.max

object ImageProcessingUtils {

    /**
     * Safely decodes a sampled bitmap from a content Uri to prevent OutOfMemoryError.
     * Manages input stream resources with automatic closing.
     */
    fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int = 1800,
        reqHeight: Int = 2400
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Read dimensions without allocating bitmap memory
            openStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return null
            }

            // Calculate optimal sample size (power of 2)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            // Decode actual bitmap
            openStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun openStream(context: Context, uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }

    /**
     * Applies document filters (Grayscale, High Contrast B&W, or Auto-Enhance).
     * Returns a new filtered bitmap or the original if unchanged.
     */
    fun applyFilter(source: Bitmap, filter: DocumentFilter): Bitmap {
        if (filter == DocumentFilter.ORIGINAL) return source

        return try {
            val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            val colorMatrix = when (filter) {
                DocumentFilter.ORIGINAL -> ColorMatrix()
                DocumentFilter.ENHANCED -> {
                    // Increase contrast and slight brightness
                    val cm = ColorMatrix()
                    val contrast = 1.25f
                    val brightness = 10f
                    cm.set(floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    cm
                }
                DocumentFilter.GRAYSCALE -> {
                    val cm = ColorMatrix()
                    cm.setSaturation(0f)
                    cm
                }
                DocumentFilter.BLACK_WHITE -> {
                    // High-contrast binary-like document threshold
                    val cm = ColorMatrix()
                    cm.setSaturation(0f)
                    val contrastMatrix = ColorMatrix()
                    val scale = 2.0f
                    val translate = (-0.5f * scale + 0.5f) * 255f
                    contrastMatrix.set(floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    cm.postConcat(contrastMatrix)
                    cm
                }
            }

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(source, 0f, 0f, paint)
            result
        } catch (e: Exception) {
            e.printStackTrace()
            source
        }
    }
}
