package ru.glyph.screen.folder.ui.composable.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import ru.glyph.design.components.FolderCardUiModel
import ru.glyph.design.components.NoteCardUiModel

@Immutable
internal data class FolderScreenUiState(
    val folderName: String = "",
    val folderColor: Color = Color.Transparent,
    val subfolders: List<FolderCardUiModel> = emptyList(),
    val notes: List<NoteCardUiModel> = emptyList(),
    val isReady: Boolean = false,
)
