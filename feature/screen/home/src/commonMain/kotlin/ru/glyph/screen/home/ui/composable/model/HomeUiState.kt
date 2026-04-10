package ru.glyph.screen.home.ui.composable.model

import androidx.compose.runtime.Immutable
import ru.glyph.design.theme.GlyphFolderColors

@Immutable
internal data class HomeUiState(
    val folders: List<FolderUiModel> = emptyList(),
    val recentNotes: List<NoteUiModel> = emptyList(),
) {

    companion object {

        fun forPreview() = HomeUiState(
            folders = listOf(
                FolderUiModel.forPreview(),
                FolderUiModel(id = "2", name = "Личное", noteCount = 8, color = GlyphFolderColors.Purple),
                FolderUiModel(id = "3", name = "Идеи", noteCount = 24, color = GlyphFolderColors.Green),
            ),
            recentNotes = listOf(
                NoteUiModel.forPreview(),
                NoteUiModel(id = 2L, title = "Архитектура нового модуля", updatedAt = 1_700_000_100_000L, tags = listOf("#разработка")),
                NoteUiModel(id = 3L, title = "Список книг для чтения", updatedAt = 1_699_000_000_000L, tags = listOf("#личное", "#книги")),
            ),
        )
    }
}
