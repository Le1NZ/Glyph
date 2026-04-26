package ru.glyph.screen.note.ui.state

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface NoteUiState {

    data object Loading : NoteUiState

    data class Editing(
        val title: String,
        val content: String,
        val isPreviewMode: Boolean,
    ) : NoteUiState
}
