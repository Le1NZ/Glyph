package ru.glyph.share_bottom_sheet.impl

internal data class ShareNoteUiState(
    val emailInput: String = "",
    val shares: List<NoteShareDto> = emptyList(),
    val isLoading: Boolean = false,
    val isAdding: Boolean = false,
    val error: ShareError? = null,
) {
    enum class ShareError {
        /** Generic network / server error. */
        GENERIC,
        /** The entered email is not registered in the app yet. */
        USER_NOT_FOUND,
    }
}