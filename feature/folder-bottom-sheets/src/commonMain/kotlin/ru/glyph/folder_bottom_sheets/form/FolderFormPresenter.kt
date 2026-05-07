package ru.glyph.folder_bottom_sheets.form

import androidx.compose.runtime.Stable
import ru.glyph.navigation.api.Navigator

internal interface FolderFormPresenter {

    fun onSave(name: String)
    fun onCancel()

    @Stable
    fun interface Factory {

        fun create(
            onSave: (name: String) -> Unit,
        ): FolderFormPresenter
    }
}

internal class FolderFormPresenterImpl(
    private val onSaveAccepted: (name: String) -> Unit,
    navigatorLazy: Lazy<Navigator>,
) : FolderFormPresenter {

    private val navigator by navigatorLazy

    override fun onSave(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        onSaveAccepted(trimmed)
        navigator.hideOverlay()
    }

    override fun onCancel() {
        navigator.hideOverlay()
    }
}

internal class FolderFormPresenterPreview : FolderFormPresenter {
    override fun onSave(name: String) = Unit
    override fun onCancel() = Unit
}
