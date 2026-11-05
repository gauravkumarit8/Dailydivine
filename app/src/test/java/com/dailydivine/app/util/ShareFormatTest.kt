package com.dailydivine.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareFormatTest {

    @Test fun dimensionsMatchThePrd() {
        assertEquals(1080, ShareFormat.SQUARE.width); assertEquals(1080, ShareFormat.SQUARE.height)
        assertEquals(1080, ShareFormat.STORY.width); assertEquals(1920, ShareFormat.STORY.height)
    }

    @Test fun theLongestVerseNeverCrossesTheWatermark() {
        for (f in ShareFormat.values()) {
            val bottom = f.blockBottom(f.maxVerseHeight)
            assertTrue("${f.name}: verse block bottom $bottom reaches the watermark at ${f.watermarkBaseline}",
                bottom < f.watermarkBaseline - 20f)
        }
    }

    @Test fun theLongestVerseStaysBelowTheTopSafeArea() {
        for (f in ShareFormat.values()) {
            val top = f.blockTop(f.maxVerseHeight)
            assertTrue("${f.name}: verse block top $top is above the safe area ${f.minTop}", top >= f.minTop)
        }
    }

    @Test fun theWatermarkStaysInsideTheImage() {
        for (f in ShareFormat.values()) assertTrue(f.watermarkBaseline < f.height)
    }

    @Test fun storyKeepsTheWatermarkAboveTheBottomUiOfStoryApps() {
        // Story apps cover roughly the bottom 340 px of 1920 with their own controls.
        assertTrue(ShareFormat.STORY.watermarkBaseline <= 1920 - 340)
    }

    @Test fun aShortVerseIsCentred() {
        val f = ShareFormat.STORY
        assertEquals(f.height / 2f, f.blockTop(200) + (200 + ShareFormat.GAP_AFTER_VERSE) / 2f, 0.01f)
    }
}
