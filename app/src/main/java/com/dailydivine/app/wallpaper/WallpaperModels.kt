package com.dailydivine.app.wallpaper

/** Which screen(s) the daily wallpaper is applied to. Stored by [name] in DataStore. */
enum class WallpaperTarget(val label: String, val home: Boolean, val lock: Boolean) {
    OFF("Off", false, false),
    HOME("Home screen", true, false),
    LOCK("Lock screen", false, true),
    BOTH("Both", true, true);

    companion object {
        /** Unknown or missing values fall back to OFF: never apply a wallpaper by accident. */
        fun fromName(name: String?): WallpaperTarget = values().firstOrNull { it.name == name } ?: OFF
    }
}

/** Where the verse sits vertically. Home screens are busy with icons and lock screens have a
 *  clock near the top, so the user can move the text. [centerFraction] is the verse block's
 *  vertical centre as a fraction of the screen height. */
enum class TextPosition(val label: String, val centerFraction: Float) {
    UPPER("Upper", 0.32f),
    MIDDLE("Middle", 0.50f),
    LOWER("Lower", 0.68f);

    companion object {
        fun fromName(name: String?): TextPosition = values().firstOrNull { it.name == name } ?: MIDDLE
    }
}

/** Pure rules, kept free of Android so they are unit-tested. */
object WallpaperRules {
    /** Update when the feature is on and today's wallpaper has not been applied yet. */
    fun needsUpdate(target: WallpaperTarget, lastUpdatedEpochDay: Long, todayEpochDay: Long): Boolean =
        target != WallpaperTarget.OFF && lastUpdatedEpochDay != todayEpochDay

    /**
     * BitmapFactory inSampleSize (a power of two) for importing a user's photo: as much
     * downsampling as possible while the long edge stays >= [maxSide], and never more than
     * [maxPixels] decoded at once (a 108 MP phone photo would otherwise be ~430 MB).
     */
    fun sampleSize(width: Int, height: Int, maxSide: Int = 3200, maxPixels: Long = 12_000_000L): Int {
        var s = 1
        while (maxOf(width, height) / (s * 2) >= maxSide) s *= 2
        while ((width / s).toLong() * (height / s) > maxPixels) s *= 2
        return s
    }
}
