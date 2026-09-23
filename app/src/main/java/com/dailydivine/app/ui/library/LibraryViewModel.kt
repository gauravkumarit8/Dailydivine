package com.dailydivine.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.entity.Verse
import com.dailydivine.app.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    bookmarkRepository: BookmarkRepository
) : ViewModel() {

    /** F007-R03: "Bookmarked verses accessible from Library > Favorites."
     *  Category browsing/search (the rest of Screen S09) is Sprint 6 scope,
     *  not attempted here -- see LibraryScreen's own doc comment. */
    val bookmarkedVerses: StateFlow<List<Verse>> = bookmarkRepository.getBookmarkedVerses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
