package com.dailydivine.app.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.ColorUtils

/**
 * Draws the daily wallpaper: the verse over either the religion's gradient or the user's
 * own photo (centre-cropped, dimmed so white text stays readable). The verse font shrinks
 * until the block fits, because some passages run to 10+ lines.
 */
class WallpaperRenderer {

    fun render(
        width: Int,
        height: Int,
        verseText: String,
        reference: String,
        primaryArgb: Int,
        background: Bitmap?,
        position: TextPosition
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (background != null) {
            drawCenterCrop(canvas, background, width, height)
            // ~45% black over the whole photo so text is legible on bright images too.
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply { color = Color.argb(115, 0, 0, 0) })
        } else {
            val bottom = ColorUtils.blendARGB(primaryArgb, Color.BLACK, 0.55f)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, height.toFloat(), primaryArgb, bottom, Shader.TileMode.CLAMP)
            })
            // A soft glow near the top corner so the gradient is not flat.
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
                shader = RadialGradient(
                    width * 0.8f, height * 0.12f, width * 0.9f,
                    intArrayOf(0x33FFFFFF, 0x00FFFFFF), null, Shader.TileMode.CLAMP
                )
            })
        }

        val textWidth = (width * 0.80f).toInt()
        val verse = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.SERIF
            textSize = width * 0.058f
            setShadowLayer(width * 0.006f, 0f, width * 0.002f, 0x99000000.toInt())
        }
        val quoted = "\u201C$verseText\u201D"
        val maxBlockHeight = (height * 0.40f).toInt()
        var verseLayout = layoutOf(quoted, verse, textWidth)
        while (verseLayout.height > maxBlockHeight && verse.textSize > width * 0.030f) {
            verse.textSize -= width * 0.002f
            verseLayout = layoutOf(quoted, verse, textWidth)
        }

        val ref = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xE6FFFFFF.toInt()
            typeface = Typeface.SANS_SERIF
            textSize = maxOf(width * 0.026f, verse.textSize * 0.55f)
            setShadowLayer(width * 0.005f, 0f, width * 0.002f, 0x99000000.toInt())
        }
        val refLayout = layoutOf("\u2014 $reference", ref, textWidth)

        val gap = width * 0.04f
        val blockHeight = verseLayout.height + gap + refLayout.height
        val top = (height * position.centerFraction - blockHeight / 2f)
            .coerceIn(height * 0.12f, maxOf(height * 0.12f, height * 0.88f - blockHeight))

        canvas.save()
        canvas.translate((width - textWidth) / 2f, top)
        verseLayout.draw(canvas)
        canvas.translate(0f, verseLayout.height + gap)
        refLayout.draw(canvas)
        canvas.restore()
        return bitmap
    }

    private fun drawCenterCrop(canvas: Canvas, source: Bitmap, width: Int, height: Int) {
        val scale = maxOf(width / source.width.toFloat(), height / source.height.toFloat())
        val dx = (width - source.width * scale) / 2f
        val dy = (height - source.height * scale) / 2f
        val matrix = Matrix().apply { postScale(scale, scale); postTranslate(dx, dy) }
        canvas.drawBitmap(source, matrix, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
    }

    private fun layoutOf(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.15f)
            .build()
}
