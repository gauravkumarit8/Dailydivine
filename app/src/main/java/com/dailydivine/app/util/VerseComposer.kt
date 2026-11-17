package com.dailydivine.app.util

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan

/** Puts a verse's original-language text (Sanskrit) above its meaning for the share image and the wallpaper. */
object VerseComposer {
    private const val ORIGINAL_SCALE = 0.86f
    private const val ORIGINAL_COLOR = 0xFFFFE6C2.toInt() // a warm cream, so it reads as a different voice than the white meaning

    /** [meaning] unchanged when there is no [original]. The caller's shrink-to-fit loops work on the result as before. */
    fun withOriginal(original: String, meaning: String): CharSequence {
        val sanskrit = original.trim()
        if (sanskrit.isEmpty()) return meaning
        return SpannableStringBuilder().apply {
            append(sanskrit)
            setSpan(RelativeSizeSpan(ORIGINAL_SCALE), 0, sanskrit.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ORIGINAL_COLOR), 0, sanskrit.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("\n\n")
            append(meaning)
        }
    }
}
