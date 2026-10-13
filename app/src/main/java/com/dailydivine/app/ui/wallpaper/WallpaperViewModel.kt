package com.dailydivine.app.ui.wallpaper

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.wallpaper.TextPosition
import com.dailydivine.app.wallpaper.WallpaperBackgroundStore
import com.dailydivine.app.wallpaper.WallpaperTarget
import com.dailydivine.app.wallpaper.WallpaperUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WallpaperUiState(
    val target: WallpaperTarget = WallpaperTarget.OFF,
    val useCustom: Boolean = false,
    val hasCustomImage: Boolean = false,
    val hour: Int = 5,
    val minute: Int = 0,
    val position: TextPosition = TextPosition.MIDDLE,
    val lastUpdatedEpochDay: Long = -1L,
    val lastAttemptMillis: Long = 0L,
    val lastAttemptSource: String = "",
    val lastAttemptResult: String = "",
    val preview: Bitmap? = null,
    val busy: Boolean = false,
    /** One-shot message for a Toast; cleared once shown. */
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val updater: WallpaperUpdater,
    private val store: WallpaperBackgroundStore
) : ViewModel() {

    // Declared above init (a property below an init block that uses it would be null there).
    private val _state = MutableStateFlow(WallpaperUiState(hasCustomImage = store.hasCustom()))
    val state: StateFlow<WallpaperUiState> = _state.asStateFlow()

    /** Bumped when the stored photo changes, so the preview re-renders. */
    private val imageVersion = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            userPreferences.state.collect { p ->
                _state.value = _state.value.copy(
                    target = WallpaperTarget.fromName(p.wallpaperTarget),
                    useCustom = p.wallpaperUseCustom,
                    hour = p.wallpaperHour,
                    minute = p.wallpaperMinute,
                    position = TextPosition.fromName(p.wallpaperTextPosition),
                    lastUpdatedEpochDay = p.wallpaperLastEpochDay,
                    lastAttemptMillis = p.wallpaperLastAttemptMillis,
                    lastAttemptSource = p.wallpaperLastAttemptSource,
                    lastAttemptResult = p.wallpaperLastAttemptResult
                )
            }
        }
        // Re-render the preview whenever something that affects the picture changes.
        viewModelScope.launch {
            combine(
                userPreferences.state.map { Triple(it.religionId, it.wallpaperUseCustom, it.wallpaperTextPosition) }
                    .distinctUntilChanged(),
                imageVersion
            ) { key, version -> key to version }
                .collectLatest { regeneratePreview() }
        }
    }

    private suspend fun regeneratePreview() {
        val (w, h) = updater.deviceSize()
        val previewW = 540
        val previewH = (previewW * h.toFloat() / w).toInt()
        val bitmap = updater.renderBitmap(previewW, previewH)
        _state.value = _state.value.copy(preview = bitmap)
    }

    fun setTarget(target: WallpaperTarget) {
        viewModelScope.launch {
            userPreferences.setWallpaperTarget(target.name)
            if (target != WallpaperTarget.OFF) applyNow() // show the user the result straight away
        }
    }

    fun setPosition(position: TextPosition) { viewModelScope.launch { userPreferences.setWallpaperTextPosition(position.name) } }
    fun setTime(hour: Int, minute: Int) { viewModelScope.launch { userPreferences.setWallpaperTime(hour, minute) } }
    fun useThemeBackground() { viewModelScope.launch { userPreferences.setWallpaperUseCustom(false) } }
    fun usePhotoBackground() { viewModelScope.launch { userPreferences.setWallpaperUseCustom(true) } }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true)
            val ok = store.importFrom(uri)
            if (ok) {
                userPreferences.setWallpaperUseCustom(true)
                imageVersion.value = imageVersion.value + 1
            }
            _state.value = _state.value.copy(
                busy = false,
                hasCustomImage = store.hasCustom(),
                message = if (ok) "Photo saved. Tap \u201CUpdate wallpaper now\u201D to use it." else "Couldn't use that image. Try another one."
            )
        }
    }

    fun removePhoto() {
        viewModelScope.launch {
            store.delete()
            userPreferences.setWallpaperUseCustom(false)
            imageVersion.value = imageVersion.value + 1
            _state.value = _state.value.copy(hasCustomImage = false)
        }
    }

    fun applyNow() {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true)
            val result = updater.updateNow()
            _state.value = _state.value.copy(
                busy = false,
                message = when (result) {
                    WallpaperUpdater.Result.APPLIED -> "Wallpaper updated"
                    WallpaperUpdater.Result.NO_VERSE -> "No verse is available yet for your path"
                    WallpaperUpdater.Result.DISABLED -> "Choose Home screen, Lock screen or Both first"
                    WallpaperUpdater.Result.FAILED -> "This phone wouldn't let DailyDivine change the wallpaper"
                    WallpaperUpdater.Result.UP_TO_DATE -> null
                }
            )
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(message = null) }
}
