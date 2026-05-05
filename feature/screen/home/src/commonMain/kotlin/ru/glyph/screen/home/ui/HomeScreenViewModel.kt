package ru.glyph.screen.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.glyph.database.api.NotesRepository
import ru.glyph.model.Note
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.home.ui.composable.model.HomeUiState
import ru.glyph.screen.home.ui.composable.model.NoteUiModel
import ru.glyph.sync.api.SyncBootstrap

internal class HomeScreenViewModel(
    private val navigator: Navigator,
    private val notesRepository: NotesRepository,
    private val syncBootstrap: SyncBootstrap,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)

    val state = combine(
        _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {
                notesRepository.observeAll()
            } else {
                notesRepository.search(query)
            }
        },
        _isRefreshing,
        _searchQuery,
    ) { notes, isRefreshing, query ->
        HomeUiState(
            recentNotes = notes.map { it.toUiModel() },
            isRefreshing = isRefreshing,
            searchQuery = query,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onProfileClick() {
        navigator.navigateTo(Screen.Profile)
    }

    fun onNoteClick(id: String) {
        navigator.navigateTo(Screen.Note(id))
    }

    fun onCreateNoteClick() {
        viewModelScope.launch {
            val id = notesRepository.create()
            navigator.navigateTo(Screen.Note(id))
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncBootstrap.pullAll()
            _isRefreshing.value = false
        }
    }

    private fun Note.toUiModel() = NoteUiModel(
        id = id,
        title = title,
        updatedAt = updatedAt,
        tags = emptyList(),
    )
}
