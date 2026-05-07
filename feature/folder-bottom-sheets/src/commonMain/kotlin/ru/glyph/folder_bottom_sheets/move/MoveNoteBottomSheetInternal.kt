package ru.glyph.folder_bottom_sheets.move

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.glyph.model.Folder

@Composable
internal fun MoveNoteBottomSheetInternal(
    presenterFactory: MoveNotePresenter.Factory,
    folders: List<Folder>,
    currentFolderId: String?,
    onMove: (folderId: String?) -> Unit,
) {
    val presenter = remember(onMove) {
        presenterFactory.create(onMove)
    }

    MoveNoteBottomSheetContent(
        presenter = presenter,
        folders = folders,
        currentFolderId = currentFolderId,
    )
}
