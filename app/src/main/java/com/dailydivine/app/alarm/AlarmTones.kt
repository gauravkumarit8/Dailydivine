package com.dailydivine.app.alarm

import androidx.annotation.RawRes
import com.dailydivine.app.R

/**
 * Catalog of bundled alarm tones (PRD 6.2 / F004-R09). Explicit R.raw
 * references (not Resources.getIdentifier by name) so release-build resource
 * shrinking can never strip a tone that is only referenced dynamically.
 *
 * Religion ids match ui/theme/Color.kt: 1 Hinduism, 2 Christianity, 3 Islam,
 * 4 Buddhism, 5 Sikhism, 6 Judaism, null = universal.
 *
 * Not yet included (need licensed real recordings, see tools/generate_tones.py):
 * T02 Om Chanting, T05 Choir Hymn, T06 Azaan, T09 Shabad, T10 Shofar.
 */
data class AlarmToneInfo(
    val id: String,
    val name: String,
    val religionId: Int?,
    @RawRes val resId: Int
)

object AlarmTones {
    const val DEFAULT_ID = "temple_bell"

    val all: List<AlarmToneInfo> = listOf(
        AlarmToneInfo("temple_bell", "Temple Bell", 1, R.raw.temple_bell),
        AlarmToneInfo("bamboo_flute", "Bamboo Flute", 1, R.raw.bamboo_flute),
        AlarmToneInfo("church_bell", "Church Bell", 2, R.raw.church_bell),
        AlarmToneInfo("tibetan_bowl", "Tibetan Bowl", 4, R.raw.tibetan_bowl),
        AlarmToneInfo("meditation_gong", "Meditation Gong", 4, R.raw.meditation_gong),
        AlarmToneInfo("soft_chimes", "Soft Chimes", null, R.raw.soft_chimes),
        AlarmToneInfo("gentle_harp", "Gentle Harp", null, R.raw.gentle_harp),
        AlarmToneInfo("peaceful_piano", "Peaceful Piano", null, R.raw.peaceful_piano),
        AlarmToneInfo("nature_birds", "Nature: Birds", null, R.raw.nature_birds),
        AlarmToneInfo("nature_rain", "Nature: Rain", null, R.raw.nature_rain),
        AlarmToneInfo("nature_ocean", "Nature: Ocean", null, R.raw.nature_ocean)
    )

    fun byId(id: String?): AlarmToneInfo =
        all.firstOrNull { it.id == id } ?: all.first { it.id == DEFAULT_ID }

    @RawRes
    fun resIdFor(id: String?): Int = byId(id).resId

    /** Tones for the user's religion first, then universal, then the rest. */
    fun orderedFor(religionId: Int?): List<AlarmToneInfo> =
        all.sortedBy { t ->
            when (t.religionId) {
                religionId -> 0
                null -> 1
                else -> 2
            }
        }
}
