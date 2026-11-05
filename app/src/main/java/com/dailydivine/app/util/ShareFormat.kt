package com.dailydivine.app.util

/**
 * Share-image formats (PRD F008-R05 square, F008-R06 story). Pure so the layout rules are unit-tested.
 *
 * [maxVerseHeight] is the tallest the verse text block may be; the block is centred vertically with the
 * source line under it, and the app watermark sits at [watermarkBaseline]. For the story format the verse
 * must stay inside the safe area that Instagram and WhatsApp leave free of their own buttons (roughly the
 * top 340 px and bottom 340 px of 1920), hence the larger margins.
 */
enum class ShareFormat(
    val width: Int,
    val height: Int,
    val baseTextSize: Float,
    val maxVerseHeight: Int,
    val watermarkBaseline: Float,
    /** Nothing but the background may be drawn above this line. */
    val minTop: Int
) {
    SQUARE(width = 1080, height = 1080, baseTextSize = 56f, maxVerseHeight = 680, watermarkBaseline = 1016f, minTop = 100),
    STORY(width = 1080, height = 1920, baseTextSize = 64f, maxVerseHeight = 1000, watermarkBaseline = 1560f, minTop = 340);

    companion object {
        /** Gap between the verse and the source line (also used by the generator). */
        const val GAP_AFTER_VERSE = 48f

        /** Approximate height of the source line under the verse. */
        const val SOURCE_LINE_HEIGHT = 12f
    }

    /** Where the centred verse block starts when it is [verseHeight] tall. */
    fun blockTop(verseHeight: Int): Float = (height - (verseHeight + GAP_AFTER_VERSE)) / 2f

    /** Where the source line ends when the verse is [verseHeight] tall. */
    fun blockBottom(verseHeight: Int): Float = blockTop(verseHeight) + verseHeight + GAP_AFTER_VERSE + SOURCE_LINE_HEIGHT
}
