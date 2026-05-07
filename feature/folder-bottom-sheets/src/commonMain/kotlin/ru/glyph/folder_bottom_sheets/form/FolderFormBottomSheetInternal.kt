package ru.glyph.folder_bottom_sheets.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.glyph.navigation.api.model.BottomSheet

@Composable
internal fun FolderFormBottomSheetInternal(
    presenterFactory: FolderFormPresenter.Factory,
    mode: BottomSheet.FolderForm.Mode,
    initialName: String,
    onSave: (name: String) -> Unit,
) {
    val presenter = remember(onSave) {
        presenterFactory.create(onSave)
    }

    FolderFormBottomSheetContent(
        presenter = presenter,
        mode = mode,
        initialName = initialName,
    )
}
