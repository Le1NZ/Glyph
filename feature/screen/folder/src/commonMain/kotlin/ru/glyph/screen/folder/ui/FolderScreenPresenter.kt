package ru.glyph.screen.folder.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.glyph.screen.folder.ui.composable.model.FolderScreenUiState

internal interface FolderScreenPresenter {
    val state: StateFlow<FolderScreenUiState>

    fun onBackClick()
    fun onSubfolderClick(id: String)
    fun onSubfolderActionsClick(id: String)
    fun onNoteClick(id: String)
    fun onCreateNoteClick()
    fun onCreateSubfolderClick()
    fun onCurrentFolderActionsClick()
}

internal class FolderScreenPresenterImpl(
    private val viewModel: FolderScreenViewModel,
) : FolderScreenPresenter {
    override val state = viewModel.state

    override fun onBackClick() = viewModel.onBackClick()
    override fun onSubfolderClick(id: String) = viewModel.onSubfolderClick(id)
    override fun onSubfolderActionsClick(id: String) = viewModel.onSubfolderActionsClick(id)
    override fun onNoteClick(id: String) = viewModel.onNoteClick(id)
    override fun onCreateNoteClick() = viewModel.onCreateNoteClick()
    override fun onCreateSubfolderClick() = viewModel.onCreateSubfolderClick()
    override fun onCurrentFolderActionsClick() = viewModel.onCurrentFolderActionsClick()
}

internal class FolderScreenPresenterPreview : FolderScreenPresenter {
    override val state = MutableStateFlow(FolderScreenUiState())
    override fun onBackClick() = Unit
    override fun onSubfolderClick(id: String) = Unit
    override fun onSubfolderActionsClick(id: String) = Unit
    override fun onNoteClick(id: String) = Unit
    override fun onCreateNoteClick() = Unit
    override fun onCreateSubfolderClick() = Unit
    override fun onCurrentFolderActionsClick() = Unit
}
