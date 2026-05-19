package ru.glyph.screen.folder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.glyph.database.api.FoldersRepository
import ru.glyph.database.api.NotesRepository
import ru.glyph.design.components.FolderCardUiModel
import ru.glyph.design.components.NoteCardUiModel
import ru.glyph.design.theme.toGlyphColor
import ru.glyph.model.Folder
import ru.glyph.model.FolderPermission
import ru.glyph.model.Note
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.folder.ui.composable.model.FolderScreenUiState
import ru.glyph.string.resources.folder_delete_confirmation
import ru.glyph.string.resources.Res as StringRes

internal class FolderScreenViewModel(
    private val folderId: String,
    private val notesRepository: NotesRepository,
    private val foldersRepository: FoldersRepository,
    private val navigator: Navigator,
) : ViewModel() {

    private val folderFlow = foldersRepository.observeAll()

    val state: StateFlow<FolderScreenUiState> = combine(
        folderFlow,
        foldersRepository.observeByParent(folderId),
        notesRepository.observeByFolder(folderId),
        notesRepository.observeFolderCounts(),
    ) { allFolders, subfolders, notes, noteCounts ->
        val current = allFolders.firstOrNull { it.id == folderId }
        if (current == null) {
            return@combine FolderScreenUiState(isReady = true)
        }
        FolderScreenUiState(
            folderName = current.name,
            folderColor = current.color.toGlyphColor(),
            subfolders = subfolders.map { it.toUiModel(noteCount = noteCounts[it.id] ?: 0) },
            notes = notes.map { it.toUiModel() },
            isReady = true,
            isReadOnly = current.permission == FolderPermission.READ,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FolderScreenUiState(),
    )

    fun onBackClick() {
        navigator.popBackStack()
    }

    fun onSubfolderClick(id: String) {
        navigator.navigateTo(Screen.Folder(id))
    }

    fun onNoteClick(id: String) {
        navigator.navigateTo(Screen.Note(id))
    }

    fun onCreateNoteClick() {
        if (state.value.isReadOnly) return
        viewModelScope.launch {
            val id = notesRepository.create(folderId = folderId)
            navigator.navigateTo(Screen.Note(id))
        }
    }

    fun onCreateSubfolderClick() {
        if (state.value.isReadOnly) return
        navigator.showOverlay(
            overlay = BottomSheet.FolderForm(
                mode = BottomSheet.FolderForm.Mode.Create,
                onSave = { name ->
                    viewModelScope.launch {
                        foldersRepository.create(name = name, parentFolderId = folderId)
                    }
                },
            ),
        )
    }

    fun onCurrentFolderActionsClick() {
        if (state.value.isReadOnly) return
        viewModelScope.launch {
            val folder = foldersRepository.getById(folderId) ?: return@launch
            showActions(folder)
        }
    }

    fun onSubfolderActionsClick(id: String) {
        viewModelScope.launch {
            val folder = foldersRepository.getById(id) ?: return@launch
            if (folder.permission == FolderPermission.READ) return@launch
            showActions(folder)
        }
    }

    private fun showActions(folder: Folder) {
        navigator.showOverlay(
            overlay = BottomSheet.FolderActions(
                folder = folder,
                onRename = { showRenameSheet(folder) },
                onDelete = { showDeleteConfirmation(folder) },
            ),
        )
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
                    viewModelScope.launch {
                        if (folder.id == folderId) {
                            navigator.popBackStack()
                        }
                        foldersRepository.delete(folder.id)
                    }
                },
            ),
        )
    }

    private fun Folder.toUiModel(noteCount: Int) = FolderCardUiModel(
        id = id,
        name = name,
        noteCount = noteCount,
        color = color.toGlyphColor(),
        isReadOnly = permission == FolderPermission.READ,
    )

    private fun Note.toUiModel() = NoteCardUiModel(
        id = id,
        title = title,
        updatedAt = updatedAt,
    )
}
