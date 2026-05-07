package ru.glyph.folder_bottom_sheets.actions

import androidx.compose.runtime.Stable
import ru.glyph.navigation.api.Navigator

internal interface FolderActionsPresenter {

    fun onRename()
    fun onDelete()
    fun onDismiss()

    @Stable
    fun interface Factory {

        fun create(
            onRename: () -> Unit,
            onDelete: () -> Unit,
        ): FolderActionsPresenter
    }
}

internal class FolderActionsPresenterImpl(
    private val onRenameAccepted: () -> Unit,
    private val onDeleteAccepted: () -> Unit,
    navigatorLazy: Lazy<Navigator>,
) : FolderActionsPresenter {

    private val navigator by navigatorLazy

    override fun onRename() {
        navigator.hideOverlay()
        onRenameAccepted()
    }

    override fun onDelete() {
        navigator.hideOverlay()
        onDeleteAccepted()
    }

    override fun onDismiss() {
        navigator.hideOverlay()
    }
}

internal class FolderActionsPresenterPreview : FolderActionsPresenter {
    override fun onRename() = Unit
    override fun onDelete() = Unit
    override fun onDismiss() = Unit
}
