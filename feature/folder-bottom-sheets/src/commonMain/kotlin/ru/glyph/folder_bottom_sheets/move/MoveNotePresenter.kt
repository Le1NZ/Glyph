package ru.glyph.folder_bottom_sheets.move

import androidx.compose.runtime.Stable
import ru.glyph.navigation.api.Navigator

internal interface MoveNotePresenter {

    fun onSelect(folderId: String?)
    fun onCancel()

    @Stable
    fun interface Factory {

        fun create(
            onMove: (folderId: String?) -> Unit,
        ): MoveNotePresenter
    }
}

internal class MoveNotePresenterImpl(
    private val onMoveAccepted: (folderId: String?) -> Unit,
    navigatorLazy: Lazy<Navigator>,
) : MoveNotePresenter {

    private val navigator by navigatorLazy

    override fun onSelect(folderId: String?) {
        onMoveAccepted(folderId)
        navigator.hideOverlay()
    }

    override fun onCancel() {
        navigator.hideOverlay()
    }
}

internal class MoveNotePresenterPreview : MoveNotePresenter {
    override fun onSelect(folderId: String?) = Unit
    override fun onCancel() = Unit
}
