package com.dailydivine.app.util

/**
 * Which cached share images may be deleted. Every share wrote a new 1080x1080 PNG to the cache and
 * nothing ever removed it. An image is kept for an hour (the receiving app may still be reading the
 * one just shared); only our own files are ever touched. Pure so it is unit-tested.
 */
object ShareCachePolicy {
    const val FILE_PREFIX = "verse_share_"
    const val MAX_AGE_MS = 60 * 60 * 1000L

    fun isStale(fileName: String, ageMs: Long): Boolean =
        fileName.startsWith(FILE_PREFIX) && ageMs > MAX_AGE_MS
}
