package com.dailydivine.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * A verse as text. Poems (Arnold's Gita, for one) have hard line breaks, and their lines are often
 * longer than a phone is wide, so a wrapped line used to look like a new line ("deeds be", "equable"
 * alone). Here each source line is its own paragraph and its wrapped part is indented, so a
 * continuation can be told from a new line. Text without line breaks is drawn as plain text.
 */
@Composable
fun PoemText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    val lines = text.split('\n')
    if (lines.size > 1) {
        Text(
            buildAnnotatedString {
                lines.forEach { line ->
                    withStyle(ParagraphStyle(textIndent = TextIndent(restLine = 20.sp))) { append(line) }
                }
            },
            style = style,
            modifier = modifier
        )
    } else {
        Text(text, style = style, modifier = modifier)
    }
}
