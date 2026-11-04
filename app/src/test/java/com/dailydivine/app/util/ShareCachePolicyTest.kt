package com.dailydivine.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareCachePolicyTest {
    @Test fun oldShareImagesAreStale() {
        assertTrue(ShareCachePolicy.isStale("verse_share_1700000000000.png", ShareCachePolicy.MAX_AGE_MS + 1))
    }

    @Test fun recentShareImagesAreKeptBecauseTheReceivingAppMayStillReadThem() {
        assertFalse(ShareCachePolicy.isStale("verse_share_1700000000000.png", 5_000))
        assertFalse(ShareCachePolicy.isStale("verse_share_1700000000000.png", ShareCachePolicy.MAX_AGE_MS))
    }

    @Test fun otherFilesAreNeverTouched() {
        assertFalse(ShareCachePolicy.isStale("something_else.png", Long.MAX_VALUE))
        assertFalse(ShareCachePolicy.isStale("wallpaper_background.jpg", Long.MAX_VALUE))
    }
}
