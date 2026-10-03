package com.dailydivine.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.dao.CategoryDao
import com.dailydivine.app.data.local.dao.VerseDao
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.local.entity.Category
import com.dailydivine.app.data.local.entity.Verse
import com.dailydivine.app.data.repository.BookmarkRepository
import com.dailydivine.app.data.repository.JournalRepository
import com.dailydivine.app.domain.model.JournalHistoryItem
import com.dailydivine.app.util.ReligionMeta
import com.dailydivine.app.util.Religions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Screen S09 (PRD Section 9) state: category browsing (F006-R01/02), search
 * (F006-R04/05), verse detail (S10) and Favorites (F007-R03).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    journalRepository: JournalRepository,
    private val verseDao: VerseDao,
    categoryDao: CategoryDao,
    userPreferences: UserPreferences
) : ViewModel() {

    // Same fallback as HomeViewModel: no stored religion -> first in the registry.
    private val religionId: StateFlow<Int> = userPreferences.state
        .map { it.religionId ?: Religions.ALL.first().id }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Religions.ALL.first().id)

    val religion: StateFlow<ReligionMeta?> = religionId
        .map { Religions.byId(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categories: StateFlow<List<Category>> = religionId
        .flatMapLatest { categoryDao.getCategoriesForReligion(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _detailVerse = MutableStateFlow<Verse?>(null)
    val detailVerse: StateFlow<Verse?> = _detailVerse.asStateFlow()

    /** Search results when a query is typed, else the open category's verses. */
    val verses: StateFlow<List<Verse>> = combine(religionId, _selectedCategory, _query) { rid, cat, q ->
        Triple(rid, cat, q.trim())
    }.flatMapLatest { (rid, cat, q) ->
        when {
            q.isNotEmpty() -> verseDao.searchVerses(rid, q)
            cat != null -> verseDao.getVersesByCategory(cat.id)
            else -> flowOf(emptyList<Verse>())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedVerses: StateFlow<List<Verse>> = bookmarkRepository.getBookmarkedVerses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Past days (newest first) with each day's verse and reflection. */
    val history: StateFlow<List<JournalHistoryItem>> = journalRepository.history()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _historyDetail = MutableStateFlow<JournalHistoryItem?>(null)
    val historyDetail: StateFlow<JournalHistoryItem?> = _historyDetail.asStateFlow()
    fun showHistoryDetail(item: JournalHistoryItem) { _historyDetail.value = item }
    fun closeHistoryDetail() { _historyDetail.value = null }

    fun setQuery(q: String) { _query.value = q }
    fun openCategory(c: Category) { _selectedCategory.value = c }
    fun closeCategory() { _selectedCategory.value = null }
    fun showDetail(v: Verse) { _detailVerse.value = v }
    fun closeDetail() { _detailVerse.value = null }

    fun toggleBookmark(verse: Verse, currentlyBookmarked: Boolean) {
        viewModelScope.launch { bookmarkRepository.toggle(verse.id, currentlyBookmarked) }
    }
}
