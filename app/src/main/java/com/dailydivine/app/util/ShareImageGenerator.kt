package com.dailydivine.app.util

import com.dailydivine.app.ui.theme.darkenUntilContrast
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.dailydivine.app.ui.theme.paletteForReligionId
import java.io.File
import java.io.FileOutputStream

/**
 * F008: generates a shareable verse image (F008-R02/R04/R05/R09) and
 * returns a content:// Uri suitable for Intent.ACTION_SEND (F008-R07).
 *
 * Scope for this pass: one gradient template per religion, reusing the
 * palettes already defined in ui/theme/Color.kt (F008-R10's "auto-selected
 * based on religion theme color" without the full "10 templates" of
 * F008-R03, which is real design-asset work, not something to fabricate
 * here). Formats: 1080x1080 square (F008-R05) and 1080x1920 story (F008-R06), see ShareFormat.
 */
class ShareImageGenerator(private val context: Context) {

    fun generate(verseText: String, source: String, religionId: Int, format: ShareFormat = ShareFormat.SQUARE): Uri {
        val width = format.width
        val height = format.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val palette = paletteForReligionId(religionId)
        // All text on the image is white, so both gradient colours are darkened until white text has at
        // least 4.5:1 contrast (some tradition colours were too light for that).
        val white = listOf(androidx.compose.ui.graphics.Color.White)
        val topColor = darkenUntilContrast(palette.primary, white)
        val bottomColor = darkenUntilContrast(palette.secondary, white)
        val backgroundPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                topColor.toArgb(), bottomColor.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val horizontalPadding = 110
        val textWidth = width - horizontalPadding * 2

        val versePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = format.baseTextSize
            isAntiAlias = true
        }
        val quoted = "\u201C${VerseDisplayRules.normalize(verseText)}\u201D"
        // Long passages (some verses run 10+ lines) must still fit the square:
        // shrink the font in steps until the text block fits between the top
        // margin and the source/watermark area, down to a readable floor.
        val maxVerseHeight = format.maxVerseHeight
        var verseLayout = buildStaticLayout(quoted, versePaint, textWidth)
        while (verseLayout.height > maxVerseHeight && versePaint.textSize > 28f) {
            versePaint.textSize -= 4f
            verseLayout = buildStaticLayout(quoted, versePaint, textWidth)
        }

        val sourcePaint = TextPaint().apply {
            color = Color.WHITE
            alpha = 210
            textSize = 34f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val watermarkPaint = TextPaint().apply {
            color = Color.WHITE
            alpha = 160
            textSize = 30f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Vertically center the verse block (verse + gap + source) as a unit.
        val gapAfterVerse = ShareFormat.GAP_AFTER_VERSE
        val verseTop = format.blockTop(verseLayout.height)

        canvas.save()
        canvas.translate(horizontalPadding.toFloat(), verseTop)
        verseLayout.draw(canvas)
        canvas.restore()

        canvas.drawText("\u2014 $source", width / 2f, verseTop + verseLayout.height + gapAfterVerse, sourcePaint)

        // F008-R09: app watermark, bottom of the image.
        canvas.drawText("DailyDivine", width / 2f, format.watermarkBaseline, watermarkPaint)

        val cacheDir = File(context.cacheDir, "images").apply { mkdirs() }
        deleteStaleShares(cacheDir)
        val file = File(cacheDir, "${ShareCachePolicy.FILE_PREFIX}${format.name.lowercase()}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bitmap.recycle()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Removes our own old share images (see ShareCachePolicy); they used to pile up forever. */
    private fun deleteStaleShares(dir: File) {
        val now = System.currentTimeMillis()
        dir.listFiles()?.forEach { f ->
            if (ShareCachePolicy.isStale(f.name, now - f.lastModified())) f.delete()
        }
    }

    private fun buildStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(1.1f, 1.15f)
            .build()
}
