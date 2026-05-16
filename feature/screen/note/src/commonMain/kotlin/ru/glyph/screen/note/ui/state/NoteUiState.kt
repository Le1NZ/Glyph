package ru.glyph.screen.note.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue

@Immutable
internal sealed interface NoteUiState {

    data object Loading : NoteUiState

    data class Editing(
        val title: String,
        val content: TextFieldValue,
        val isPreviewMode: Boolean,
        val isReadOnly: Boolean,
        val isOwner: Boolean,
    ) : NoteUiState
}
