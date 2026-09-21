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

    init {
        viewModelScope.launch {
            val prefs = userPreferences.state.first()
            _startDestination.value = if (prefs.onboardingCompleted) {
                Screen.Home.route
            } else {
                Screen.Welcome.route
            }
        }
    }
}
