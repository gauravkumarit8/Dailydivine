package com.dailydivine.app.ui.theme

import androidx.compose.ui.graphics.Color

// PRD Section 20.1 — per-religion palette
data class ReligionPalette(val primary: Color, val secondary: Color, val surface: Color)

val HinduismPalette = ReligionPalette(Color(0xFFFF5722), Color(0xFFFFC107), Color(0xFFFFF3E0))
val ChristianityPalette = ReligionPalette(Color(0xFF1565C0), Color(0xFFFFD54F), Color(0xFFE3F2FD))
val IslamPalette = ReligionPalette(Color(0xFF2E7D32), Color(0xFFFFD54F), Color(0xFFE8F5E9))
val BuddhismPalette = ReligionPalette(Color(0xFF6A1B9A), Color(0xFFFFB74D), Color(0xFFF3E5F5))
val SikhismPalette = ReligionPalette(Color(0xFFE65100), Color(0xFF1565C0), Color(0xFFFFF3E0))
val JudaismPalette = ReligionPalette(Color(0xFF1565C0), Color(0xFFFFFFFF), Color(0xFFE3F2FD))
val SpiritualPalette = ReligionPalette(Color(0xFF00695C), Color(0xFFB39DDB), Color(0xFFE0F2F1))

fun paletteForReligionId(religionId: Int): ReligionPalette = when (religionId) {
    1 -> HinduismPalette
    2 -> ChristianityPalette
    3 -> IslamPalette
    4 -> BuddhismPalette
    5 -> SikhismPalette
    6 -> JudaismPalette
    else -> SpiritualPalette
}
