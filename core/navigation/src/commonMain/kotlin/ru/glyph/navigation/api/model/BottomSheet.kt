package ru.glyph.navigation.api.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import ru.glyph.model.Folder
import ru.glyph.navigation.ui.SceneStrategyImpl

sealed class BottomSheet : OverlayDestination() {

    data class Confirm(
        val text: @Composable () -> String,
        val onConfirm: () -> Unit,
    ) : BottomSheet()

    data class FolderForm(
        val mode: Mode,
        val initialName: String = "",
        val onSave: (name: String) -> Unit,
    ) : BottomSheet() {
        enum class Mode { Create, Rename }
    }

    data class FolderActions(
        val folder: Folder,
        val onRename: () -> Unit,
        val onDelete: () -> Unit,
    ) : BottomSheet()

    data class MoveNoteToFolder(
        val folders: List<Folder>,
        val currentFolderId: String?,
        val onMove: (folderId: String?) -> Unit,
    ) : BottomSheet()
}

@Immutable
data class BottomSheetMeta(
    val skipPartiallyExpanded: Boolean,
)

fun bottomSheetMetadata(
    meta: BottomSheetMeta,
): Map<String, BottomSheetMeta> {
    return SceneStrategyImpl.bottomSheet(meta)
}