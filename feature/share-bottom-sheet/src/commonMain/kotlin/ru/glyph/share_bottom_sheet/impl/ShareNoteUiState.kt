package ru.glyph.share_bottom_sheet.impl

internal data class ShareNoteUiState(
    val emailInput: String = "",
    val shares: List<NoteShareDto> = emptyList(),
    val isLoading: Boolean = false,
    val isAdding: Boolean = false,
    val error: Boolean? = null,
)