package com.dailydivine.app.util

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
 * here). 1080x1080 square only (F008-R05); the 1080x1920 story variant
 * (F008-R06) is a follow-up.
 */
class ShareImageGenerator(private val context: Context) {

    fun generate(verseText: String, source: String, religionId: Int): Uri {
        val size = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val palette = paletteForReligionId(religionId)
        val backgroundPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, size.toFloat(),
                palette.primary.toArgb(), palette.secondary.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), backgroundPaint)

        val horizontalPadding = 110
        val textWidth = size - horizontalPadding * 2

        val versePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 56f
            isAntiAlias = true
        }
        val quoted = "\u201C$verseText\u201D"
        val verseLayout = buildStaticLayout(quoted, versePaint, textWidth)

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
        val gapAfterVerse = 48f
        val blockHeight = verseLayout.height + gapAfterVerse
        val verseTop = (size - blockHeight) / 2f

        canvas.save()
        canvas.translate(horizontalPadding.toFloat(), verseTop)
        verseLayout.draw(canvas)
        canvas.restore()

        canvas.drawText("\u2014 $source", size / 2f, verseTop + verseLayout.height + gapAfterVerse, sourcePaint)

        // F008-R09: app watermark, bottom of the image.
        canvas.drawText("DailyDivine", size / 2f, size - 64f, watermarkPaint)

        val cacheDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(cacheDir, "verse_share_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bitmap.recycle()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun buildStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(1.1f, 1.15f)
            .build()
}
