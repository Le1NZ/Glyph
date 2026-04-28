package ru.glyph.screen.note.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.glyph.database.api.NotesRepository
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.screen.note.ui.state.NoteUiState
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.note_delete_confirmation
import ru.glyph.string.resources.profile_sign_out_confirmation
import ru.glyph.utils.flow.collectIn
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class NoteScreenViewModel(
    private val noteId: String,
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

        uiState
            .filterIsInstance<NoteUiState.Editing>()
            .map { it.title to it.content }
            .distinctUntilChanged()
            .debounce(1.seconds)
            .collectIn(viewModelScope) { (title, content) ->
                notesRepository.update(noteId, title, content)
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
        _uiState.value = current.copy(isPreviewMode = !current.isPreviewMode)
    }

    fun onDeleteClick() {
        navigator.showOverlay(
            overlay = BottomSheet.Confirm(
                text = { stringResource(Res.string.note_delete_confirmation) },
                onConfirm = {
                    viewModelScope.launch {
                        notesRepository.delete(noteId)
                        navigator.popBackStack()
                    }
                }
            ),
        )
    }

    fun onBackClick() {
        navigator.popBackStack()
    }

    private suspend fun updateToActual(
        state: NoteUiState.Editing,
    ) {
        notesRepository.update(
            id = noteId,
            title = state.title,
            content = state.content,
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        when (val state = uiState.value) {
            is NoteUiState.Editing -> GlobalScope.launch {
                updateToActual(state)
            }

            is NoteUiState.Loading -> Unit
        }

        super.onCleared()
    }
}
