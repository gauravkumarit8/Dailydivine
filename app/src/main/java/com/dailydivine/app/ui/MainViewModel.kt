package com.dailydivine.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    /** Null while still resolving -- MainActivity keeps the splash screen up
     *  until this becomes non-null, so NavHost is only ever composed once
     *  the real start destination is known (never flashes Welcome before
     *  jumping to Home for a returning user, or vice versa). */
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    /**
     * BUG FIX: MainActivity previously called DailyDivineTheme { ... } with
     * no religionId at all, which silently defaults to 0 -- the Spiritual
     * (teal) palette -- for every single user, regardless of which of the
     * 7 religions they actually selected. The whole point of having 7
     * distinct ReligionPalette values (ui/theme/Color.kt) was to visually
     * theme the app per-religion; that never actually happened anywhere.
     * Reactively observed (not a one-shot read) so the app re-themes itself
     * immediately if the user changes religion from Settings, without
     * needing a restart.
     */
    private val _currentReligionId = MutableStateFlow<Int?>(null)
    val currentReligionId: StateFlow<Int?> = _currentReligionId.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.state.collect { prefs -> _currentReligionId.value = prefs.religionId }
        }
        viewModelScope.launch {
            val prefs = userPreferences.state.first()
            _startDestination.value = if (prefs.onboardingCompleted) {
                Screen.Home.route
            } else {
                // Must be the nested graph's own route, not Welcome.route
                // directly -- same bug/fix as NavGraph.kt's default
                // startDestination, see Screen.kt's OnboardingGraph comment.
                Screen.OnboardingGraph.route
            }
        }
    }
}
