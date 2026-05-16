package ru.glyph.screen.home.ui.composable.model

import androidx.compose.runtime.Immutable
import ru.glyph.design.components.FolderCardUiModel
import ru.glyph.design.components.NoteCardUiModel
import ru.glyph.design.theme.GlyphFolderColors
import ru.glyph.model.Tag

@Immutable
internal data class HomeUiState(
    val folders: List<FolderCardUiModel> = emptyList(),
    val recentNotes: List<NoteCardUiModel> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val selectedTagIdsForFilter: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
) {

    companion object {

        fun forPreview() = HomeUiState(
            folders = listOf(
                FolderCardUiModel(id = "1", name = "Рабочие проекты", noteCount = 12, color = GlyphFolderColors.Blue),
                FolderCardUiModel(id = "2", name = "Личное", noteCount = 8, color = GlyphFolderColors.Purple),
                FolderCardUiModel(id = "3", name = "Идеи", noteCount = 24, color = GlyphFolderColors.Green),
            ),
            recentNotes = listOf(
                NoteCardUiModel(id = "1L", title = "Идеи для проекта", updatedAt = 1_700_000_000_000L, tags = listOf("#идеи")),
                NoteCardUiModel(id = "2L", title = "Архитектура нового модуля", updatedAt = 1_700_000_100_000L, tags = listOf("#разработка")),
                NoteCardUiModel(id = "3L", title = "Список книг для чтения", updatedAt = 1_699_000_000_000L, tags = listOf("#личное", "#книги")),
            ),
        )
    }
}
