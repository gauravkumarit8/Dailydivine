package com.dailydivine.app.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the user's chosen background photo as a private copy in app storage. A content://
 * Uri from the photo picker can stop working later (permission expiry, the source being
 * deleted), and the daily update runs in the background long after the user picked it, so
 * the image is downsampled, rotated upright per EXIF, and stored once.
 */
@Singleton
class WallpaperBackgroundStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val file: File get() = File(context.filesDir, FILE_NAME)

    fun hasCustom(): Boolean = file.exists()

    fun load(): Bitmap? = if (file.exists()) BitmapFactory.decodeFile(file.path) else null

    fun delete() {
        file.delete()
    }

    /** @return true if the photo was stored. Never throws: a bad or huge image just fails. */
    suspend fun importFrom(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext false

            val options = BitmapFactory.Options().apply {
                inSampleSize = WallpaperRules.sampleSize(bounds.outWidth, bounds.outHeight, MAX_SIDE)
            }
            var bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return@withContext false

            val rotation = resolver.openInputStream(uri)?.use { exifRotationDegrees(it) } ?: 0
            if (rotation != 0) {
                bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height,
                    Matrix().apply { postRotate(rotation.toFloat()) }, true
                )
            }
            // Cap the stored size so the daily render stays light.
            val longEdge = maxOf(bitmap.width, bitmap.height)
            if (longEdge > MAX_SIDE) {
                val scale = MAX_SIDE.toFloat() / longEdge
                bitmap = Bitmap.createScaledBitmap(
                    bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true
                )
            }
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            true
        } catch (e: Exception) {
            false
        } catch (e: OutOfMemoryError) {
            false
        }
    }

    private fun exifRotationDegrees(stream: java.io.InputStream): Int =
        when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private companion object {
        const val FILE_NAME = "wallpaper_background.jpg"
        const val MAX_SIDE = 3200
    }
}
