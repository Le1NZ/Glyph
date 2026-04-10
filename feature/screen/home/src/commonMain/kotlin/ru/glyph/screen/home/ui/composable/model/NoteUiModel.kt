package ru.glyph.screen.home.ui.composable.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class NoteUiModel(
    val id: Long,
    val title: String,
    val updatedAt: Long,
    val tags: List<String>,
) {

    companion object {

        fun forPreview() = NoteUiModel(
            id = 1L,
            title = "Title",
            updatedAt = 1_700_000_000_000L,
            tags = listOf("Tag 1", "Tag 2"),
        )
    }
}
