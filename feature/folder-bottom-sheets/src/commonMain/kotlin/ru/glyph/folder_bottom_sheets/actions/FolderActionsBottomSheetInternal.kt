package ru.glyph.folder_bottom_sheets.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.glyph.model.Folder

@Composable
internal fun FolderActionsBottomSheetInternal(
    presenterFactory: FolderActionsPresenter.Factory,
    folder: Folder,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val presenter = remember(onRename, onDelete) {
        presenterFactory.create(onRename, onDelete)
    }

    FolderActionsBottomSheetContent(
        presenter = presenter,
        folder = folder,
    )
}
