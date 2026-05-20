package ru.glyph.screen.note.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import org.jetbrains.compose.resources.stringResource
import ru.glyph.database.api.FoldersRepository
import ru.glyph.database.api.NotesRepository
import ru.glyph.database.api.TagsRepository
import ru.glyph.model.Folder
import ru.glyph.model.NotePermission
import ru.glyph.model.Tag
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.screen.note.ui.state.NoteUiState
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.note_delete_confirmation
import ru.glyph.sync.api.SyncBootstrap
import ru.glyph.utils.flow.collectIn
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class NoteScreenViewModel(
    private val noteId: String,
    private val notesRepository: NotesRepository,
    private val foldersRepository: FoldersRepository,
    private val navigator: Navigator,
    private val syncBootstrap: SyncBootstrap,
    tagsRepository: TagsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteUiState>(NoteUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _currentFolderId = MutableStateFlow<String?>(null)
    private val _currentTagIds = MutableStateFlow<List<String>>(emptyList())

    /** True once the user has made any local edit since the note was opened. */
    private var isLocallyModified = false

    val currentFolder: StateFlow<Folder?> = combine(
        _currentFolderId,
        foldersRepository.observeAll(),
    ) { id, folders ->
        if (id == null) null else folders.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentTags: StateFlow<List<Tag>> = combine(
        _currentTagIds,
        tagsRepository.observeAll(),
    ) { ids, tags ->
        tags.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val note = notesRepository.getById(noteId)
            _currentFolderId.value = note?.folderId
            _currentTagIds.value = note?.tagIds ?: emptyList()

            val isReadOnly = note?.permission == NotePermission.READ
            val isOwner = note?.permission == NotePermission.WRITE

            _uiState.value = NoteUiState.Editing(
                title = note?.title ?: "",
                content = TextFieldValue(note?.content ?: ""),
                isPreviewMode = isReadOnly,
                isReadOnly = isReadOnly,
                isOwner = isOwner,
            )

            observeRemoteUpdates(isReadOnly)
        }

        uiState
            .filterIsInstance<NoteUiState.Editing>()
            .filter { !it.isReadOnly }
            .map { Pair(it.title, it.content.text) }
            .distinctUntilChanged()
            .debounce(1.seconds)
            .collectIn(viewModelScope) { pair ->
                notesRepository.update(noteId, pair.first, pair.second)
                // Edits flushed to DB — allow remote updates to come through again
                isLocallyModified = false
            }
    }

    /**
     * Polls the server every 10 s and reflects incoming changes via the local DB observer.
     *
     * For READ notes: always updates UI (user can't edit).
     * For WRITE notes: updates UI only when the user hasn't made local edits yet,
     * so we never override in-progress typing.
     */
    private fun observeRemoteUpdates(isReadOnly: Boolean) {
        notesRepository.observeById(noteId)
            .filterNotNull()
            .distinctUntilChanged()
            .collectIn(viewModelScope) { note ->
                val current = _uiState.value as? NoteUiState.Editing ?: return@collectIn
                val contentChanged = note.title != current.title ||
                        note.content != current.content.text
                // For WRITE notes, skip UI update while the user has unsaved local edits
                if (contentChanged && !isReadOnly && isLocallyModified) return@collectIn
                if (contentChanged) {
                    _uiState.value = current.copy(
                        title = note.title,
                        content = TextFieldValue(note.content),
                    )
                }
                _currentFolderId.value = note.folderId
                _currentTagIds.value = note.tagIds
            }

        viewModelScope.launch {
            while (isActive) {
                delay(3.seconds)
                syncBootstrap.pullNote(noteId)
            }
        }
    }

    fun onTitleChange(title: String) {
        val current = _uiState.value as? NoteUiState.Editing ?: return
        if (current.isReadOnly) return
        isLocallyModified = true
        _uiState.value = current.copy(title = title)
    }

    fun onContentChange(content: TextFieldValue) {
        val current = _uiState.value as? NoteUiState.Editing ?: return
        if (current.isReadOnly) return
        isLocallyModified = true
        _uiState.value = current.copy(content = content)
    }

    fun onTogglePreview() {
        val current = _uiState.value as? NoteUiState.Editing ?: return
        if (current.isReadOnly) return
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

    fun onMoveClick() {
        viewModelScope.launch {
            val folders = foldersRepository.observeAll().first()
            navigator.showOverlay(
                overlay = BottomSheet.MoveNoteToFolder(
                    folders = folders,
                    currentFolderId = _currentFolderId.value,
                    onMove = { newFolderId ->
                        viewModelScope.launch {
                            notesRepository.setFolder(noteId, newFolderId)
                            _currentFolderId.value = newFolderId
                        }
                    },
                ),
            )
        }
    }

    fun onTagsClick() {
        navigator.showOverlay(
            overlay = BottomSheet.TagSelection(
                noteId = noteId,
                selectedTagIds = _currentTagIds.value,
                onSave = { newTagIds ->
                    viewModelScope.launch {
                        notesRepository.setTags(noteId, newTagIds)
                        _currentTagIds.value = newTagIds
                    }
                },
            ),
        )
    }

    fun onShareClick() {
        navigator.showOverlay(BottomSheet.ShareNote(noteId))
    }

    fun onAiAssistantClick() {
        val current = _uiState.value as? NoteUiState.Editing ?: return
        if (current.isReadOnly) return
        
        navigator.showOverlay(
            BottomSheet.AiAssistant(
                noteContent = current.content.text,
                onInsertText = { generatedText ->
                    val newText = if (current.content.text.isBlank()) {
                        generatedText
                    } else {
                        "${current.content.text}\n\n$generatedText"
                    }
                    onContentChange(TextFieldValue(newText, TextRange(newText.length)))
                }
            )
        )
    }

    fun onBackClick() {
        navigator.popBackStack()
    }

    private suspend fun updateToActual(
        state: NoteUiState.Editing,
    ) {
        if (state.isReadOnly) return
        notesRepository.update(
            id = noteId,
            title = state.title,
            content = state.content.text,
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
