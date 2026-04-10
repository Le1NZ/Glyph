package ru.glyph.screen.note.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.glyph.database.api.NotesRepository
import ru.glyph.navigation.api.Navigator
import ru.glyph.screen.note.ui.state.NoteUiState

@OptIn(FlowPreview::class)
internal class NoteScreenViewModel(
    private val noteId: Long,
    private val notesRepository: NotesRepository,
    private val navigator: Navigator,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteUiState>(NoteUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val note = notesRepository.getById(noteId)
            _uiState.value = NoteUiState.Editing(
                title = note?.title ?: "",
                content = note?.content ?: "",
                isPreviewMode = false,
            )
        }

        viewModelScope.launch {
            _uiState
                .filterIsInstance<NoteUiState.Editing>()
                .map { it.title to it.content }
                .distinctUntilChanged()
                .debounce(700)
                .collect { (title, content) ->
                    notesRepository.update(noteId, title, content)
                }
        }
    }

    fun onTitleChange(title: String) {
        val current = _uiState.value as? NoteUiState.Editing ?: return
        _uiState.value = current.copy(title = title)
    }

    fun onContentChange(content: String) {
        val current = _uiState.value as? NoteUiState.Editing ?: return
        _uiState.value = current.copy(content = content)
    }

    fun onTogglePreview() {
        val current = _uiState.value as? NoteUiState.Editing ?: return
        viewModelScope.launch {
            notesRepository.update(noteId, current.title, current.content)
        }
        _uiState.value = current.copy(isPreviewMode = !current.isPreviewMode)
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            notesRepository.delete(noteId)
            navigator.popBackStack()
        }
    }

    fun onBackClick() {
        val current = _uiState.value as? NoteUiState.Editing
        if (current != null) {
            viewModelScope.launch {
                notesRepository.update(noteId, current.title, current.content)
                navigator.popBackStack()
            }
        } else {
            navigator.popBackStack()
        }
    }
}
