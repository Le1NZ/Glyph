package ru.glyph.screen.home.ui.composable.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class NoteUiModel(
    val id: String,
    val title: String,
    val updatedAt: String,
    val tags: List<String>,
) {

    companion object {

        fun forPreview() = NoteUiModel(
            id = "1",
            title = "Title",
            updatedAt = "Updated at",
            tags = listOf("Tag 1", "Tag 2"),
        )
    }
}