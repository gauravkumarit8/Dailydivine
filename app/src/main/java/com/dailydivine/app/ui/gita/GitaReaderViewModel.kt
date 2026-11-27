package com.dailydivine.app.ui.gita

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.content.GitaBook
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.GitaReaderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class GitaReaderUiState(
    val loading: Boolean = true,
    val book: GitaBook = GitaBook(),
    val languageCode: String = "en"
)

@HiltViewModel
class GitaReaderViewModel @Inject constructor(
    repository: GitaReaderRepository,
    userPreferences: UserPreferences
) : ViewModel() {

    /** The book plus the verse language chosen in Settings (Hindi shows the Hindi plain-words meaning where it exists). */
    val state: StateFlow<GitaReaderUiState> = combine(
        flow { emit(repository.book()) },
        userPreferences.state.map { it.languageCode }.distinctUntilChanged()
    ) { book, language -> GitaReaderUiState(loading = false, book = book, languageCode = language) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GitaReaderUiState())
}
