package com.dailydivine.app.util

import com.dailydivine.app.data.content.VerseLocalizer

/**
 * Which languages actually have verses. Languages not listed here are shown as "Coming soon" and
 * cannot be chosen. English works for every tradition; any other language is available for one
 * tradition only, and only where [VerseLocalizer.OVERLAY_ASSETS] has a file for it (Hindi for
 * Hinduism's Bhagavad Gita verses). Add a code only together with its content.
 */
object LanguageSupport {
    private val EVERYWHERE = setOf("en")
    private val BY_RELIGION: Map<Int, Set<String>> =
        VerseLocalizer.OVERLAY_ASSETS.keys.groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }

    /** English only: what is available for every tradition. */
    fun isAvailable(code: String): Boolean = code in EVERYWHERE

    fun isAvailable(religionId: Int?, code: String): Boolean =
        code in EVERYWHERE || (religionId != null && code in BY_RELIGION[religionId].orEmpty())

    /** (display name, code) pairs the user can choose for this tradition, in the tradition's own order. */
    fun optionsFor(religionId: Int?): List<Pair<String, String>> =
        Religions.byId(religionId ?: -1)?.languages.orEmpty().filter { isAvailable(religionId, it.second) }
}
