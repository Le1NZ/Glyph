package ru.glyph.screen.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.glyph.database.api.FoldersRepository
import ru.glyph.database.api.NotesRepository
import ru.glyph.database.api.TagsRepository
import ru.glyph.design.components.FolderCardUiModel
import ru.glyph.design.components.NoteCardUiModel
import ru.glyph.design.theme.toGlyphColor
import ru.glyph.model.Folder
import ru.glyph.model.Note
import ru.glyph.model.Tag
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.home.ui.composable.model.HomeUiState
import ru.glyph.string.resources.Res as StringRes
import ru.glyph.string.resources.folder_delete_confirmation
import ru.glyph.sync.api.SyncBootstrap

import ru.glyph.model.FolderPermission

internal class HomeScreenViewModel(
    private val navigator: Navigator,
    private val notesRepository: NotesRepository,
    private val foldersRepository: FoldersRepository,
    private val syncBootstrap: SyncBootstrap,
    tagsRepository: TagsRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    private val _selectedTagIdsForFilter = MutableStateFlow<Set<String>>(emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val notesFlow = _searchQuery.flatMapLatest { query ->
        if (query.isNotBlank()) notesRepository.search(query)
        else notesRepository.observeAll()
    }

    val state = combine(
        notesFlow,
        foldersRepository.observeByParent(null),
        notesRepository.observeFolderCounts(),
        tagsRepository.observeAll(),
        _selectedTagIdsForFilter,
        _isRefreshing,
        _searchQuery,
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val notes = args[0] as List<Note>
        @Suppress("UNCHECKED_CAST")
        val rootFolders = args[1] as List<Folder>
        @Suppress("UNCHECKED_CAST")
        val counts = args[2] as Map<String, Int>
        @Suppress("UNCHECKED_CAST")
        val tags = args[3] as List<Tag>
        @Suppress("UNCHECKED_CAST")
        val selectedTags = args[4] as Set<String>
        val isRefreshing = args[5] as Boolean
        val query = args[6] as String

        val filteredNotes = if (selectedTags.isEmpty()) {
            notes
        } else {
            notes.filter { note -> selectedTags.intersect(note.tagIds.toSet()).isNotEmpty() }
        }

        HomeUiState(
            folders = rootFolders.map { it.toUiModel(noteCount = counts[it.id] ?: 0) },
            recentNotes = filteredNotes.map { it.toUiModel() },
            availableTags = tags,
            selectedTagIdsForFilter = selectedTags,
            isLoading = false,
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

    fun onTagFilterClick(tagId: String) {
        val current = _selectedTagIdsForFilter.value
        if (current.contains(tagId)) {
            _selectedTagIdsForFilter.value = current - tagId
        } else {
            _selectedTagIdsForFilter.value = current + tagId
        }
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

    fun onFolderClick(id: String) {
        navigator.navigateTo(Screen.Folder(id))
    }

    fun onCreateFolderClick() {
        navigator.showOverlay(
            overlay = BottomSheet.FolderForm(
                mode = BottomSheet.FolderForm.Mode.Create,
                onSave = { name ->
                    viewModelScope.launch { foldersRepository.create(name = name) }
                },
            ),
        )
    }

    fun onCreateTagClick() {
        navigator.showOverlay(
            overlay = BottomSheet.TagForm(
                mode = BottomSheet.TagForm.Mode.Create,
                onSave = { _, _ -> }
            ),
        )
    }

    fun onFolderActionsClick(id: String) {
        viewModelScope.launch {
            val folder = foldersRepository.getById(id) ?: return@launch
            if (folder.permission == FolderPermission.READ) return@launch
            navigator.showOverlay(
                overlay = BottomSheet.FolderActions(
                    folder = folder,
                    onRename = { showRenameSheet(folder) },
                    onDelete = { showDeleteConfirmation(folder) },
                ),
            )
        }
    }

    private fun showRenameSheet(folder: Folder) {
        navigator.showOverlay(
            overlay = BottomSheet.FolderForm(
                mode = BottomSheet.FolderForm.Mode.Rename,
                initialName = folder.name,
                onSave = { name ->
                    viewModelScope.launch { foldersRepository.rename(folder.id, name) }
                },
            ),
        )
    }

    private fun showDeleteConfirmation(folder: Folder) {
        navigator.showOverlay(
            overlay = BottomSheet.Confirm(
                text = { stringResource(StringRes.string.folder_delete_confirmation) },
                onConfirm = {
                    viewModelScope.launch { foldersRepository.delete(folder.id) }
                },
            ),
        )
    }

    private fun Note.toUiModel() = NoteCardUiModel(
        id = id,
        title = title,
        updatedAt = updatedAt,
    )

    private fun Folder.toUiModel(noteCount: Int) = FolderCardUiModel(
        id = id,
        name = name,
        noteCount = noteCount,
        color = color.toGlyphColor(),
        isReadOnly = permission == FolderPermission.READ,
    )
}
