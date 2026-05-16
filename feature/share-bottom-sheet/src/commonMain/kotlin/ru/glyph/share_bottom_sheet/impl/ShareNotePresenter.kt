package ru.glyph.share_bottom_sheet.impl

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow
import ru.glyph.model.NotePermission

internal interface ShareNotePresenter {
    val state: StateFlow<ShareNoteUiState>

    fun onEmailChanged(email: String)
    fun onAddShare()
    fun onUpdatePermission(email: String, permission: NotePermission)
    fun onRemoveShare(email: String)
    fun onRetry()

    @Stable
    fun interface Factory {
        fun create(viewModel: ShareNoteViewModel): ShareNotePresenter
    }
}

internal class ShareNotePresenterImpl(
    private val viewModel: ShareNoteViewModel,
) : ShareNotePresenter {

    override val state = viewModel.state

    override fun onEmailChanged(email: String) = viewModel.onEmailChanged(email)
    override fun onAddShare() = viewModel.onAddShare()
    override fun onUpdatePermission(email: String, permission: NotePermission) = viewModel.onUpdatePermission(email, permission)
    override fun onRemoveShare(email: String) = viewModel.onRemoveShare(email)
    override fun onRetry() = viewModel.onRetry()
}