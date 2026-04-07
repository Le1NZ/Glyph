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
                NoteUiModel(id = "2", title = "Архитектура нового модуля", updatedAt = "Вчера, 18:45", tags = listOf("#разработка")),
                NoteUiModel(id = "3", title = "Список книг для чтения", updatedAt = "2 дня назад", tags = listOf("#личное", "#книги")),
                NoteUiModel(id = "4", title = "Заметки с конференции", updatedAt = "3 дня назад", tags = listOf("#работа")),
            ),
        )
    }
}
