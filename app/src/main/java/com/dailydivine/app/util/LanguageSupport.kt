package com.dailydivine.app.util

/**
 * Which languages actually have verses. The Language step used to let people pick Hindi, Tamil,
 * Arabic and so on, and then silently showed English, which is misleading. Languages not listed
 * here are shown as "Coming soon" and cannot be chosen. Add a code here only together with its
 * content files.
 */
object LanguageSupport {
    private val AVAILABLE = setOf("en")

    fun isAvailable(code: String): Boolean = code in AVAILABLE
}
