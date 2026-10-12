package com.dailydivine.app.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.VerseRepository
import com.dailydivine.app.ui.theme.darkenUntilContrast
import com.dailydivine.app.ui.theme.paletteForReligionId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders today's verse and sets it as the home and/or lock screen wallpaper. One shared
 * instance (Singleton) with a mutex, because the daily worker, the app coming to the
 * foreground and the "Update now" button can all ask at once.
 */
@Singleton
class WallpaperUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences,
    private val verseRepository: VerseRepository,
    private val backgroundStore: WallpaperBackgroundStore
) {
    enum class Result { APPLIED, UP_TO_DATE, DISABLED, NO_VERSE, FAILED }

    private val renderer = WallpaperRenderer()
    private val mutex = Mutex()

    /** Cheap no-op unless the feature is on and today's wallpaper hasn't been applied. */
    suspend fun updateIfNeeded(): Result = mutex.withLock {
        val prefs = userPreferences.state.first()
        val target = WallpaperTarget.fromName(prefs.wallpaperTarget)
        if (target == WallpaperTarget.OFF) return Result.DISABLED
        if (!WallpaperRules.needsUpdate(target, prefs.wallpaperLastEpochDay, LocalDate.now().toEpochDay())) {
            return Result.UP_TO_DATE
        }
        applyLocked()
    }

    /** "Update now": applies regardless of whether today's was already applied. */
    suspend fun updateNow(): Result = mutex.withLock { applyLocked() }

    private suspend fun applyLocked(): Result = withContext(Dispatchers.Default) {
        val prefs = userPreferences.state.first()
        val target = WallpaperTarget.fromName(prefs.wallpaperTarget)
        if (target == WallpaperTarget.OFF) return@withContext Result.DISABLED
        val (w, h) = deviceSize()
        val bitmap = renderBitmap(w, h) ?: return@withContext Result.NO_VERSE
        try {
            var flags = 0
            if (target.home) flags = flags or WallpaperManager.FLAG_SYSTEM
            if (target.lock) flags = flags or WallpaperManager.FLAG_LOCK
            WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, flags)
            userPreferences.setWallpaperLastEpochDay(LocalDate.now().toEpochDay())
            Result.APPLIED
        } catch (e: Exception) {
            // Some phones restrict wallpaper changes (managed devices, certain theme engines).
            Log.e(TAG, "Setting the wallpaper failed", e)
            Result.FAILED
        } finally {
            bitmap.recycle()
        }
    }

    /** Today's wallpaper image at [width] x [height] from the saved settings, or null if there
     *  is no verse yet (no religion chosen, or that religion has no content). Used for the
     *  live preview and for applying. */
    suspend fun renderBitmap(width: Int, height: Int): Bitmap? = withContext(Dispatchers.Default) {
        val prefs = userPreferences.state.first()
        val religionId = prefs.religionId ?: return@withContext null
        val installDate = prefs.installEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
        val daily = verseRepository.getDailyVerse(religionId, installDate, LocalDate.now())
            ?: return@withContext null
        val background = if (prefs.wallpaperUseCustom) backgroundStore.load() else null
        // White text sits on this colour, so it must pass 4.5:1 against white.
        val primary = darkenUntilContrast(paletteForReligionId(religionId).primary, listOf(Color.White)).toArgb()
        try {
            renderer.render(
                width, height, daily.verse.translatedText, daily.verse.sourceReference,
                primary, background, TextPosition.fromName(prefs.wallpaperTextPosition)
            )
        } finally {
            background?.recycle()
        }
    }

    /** Portrait pixel size of the screen, capped so a 4K tablet doesn't build a huge bitmap. */
    fun deviceSize(): Pair<Int, Int> {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metrics)
        var w = minOf(metrics.widthPixels, metrics.heightPixels)
        var h = maxOf(metrics.widthPixels, metrics.heightPixels)
        val longEdgeCap = 3000
        if (h > longEdgeCap) {
            val scale = longEdgeCap.toFloat() / h
            w = (w * scale).toInt(); h = longEdgeCap
        }
        return w to h
    }

    private companion object { const val TAG = "WallpaperUpdater" }
}
