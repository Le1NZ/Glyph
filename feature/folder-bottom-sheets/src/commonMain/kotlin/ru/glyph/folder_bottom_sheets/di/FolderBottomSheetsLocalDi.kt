package ru.glyph.folder_bottom_sheets.di

import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import ru.glyph.folder_bottom_sheets.actions.FolderActionsBottomSheetInternal
import ru.glyph.folder_bottom_sheets.actions.FolderActionsPresenter
import ru.glyph.folder_bottom_sheets.actions.FolderActionsPresenterImpl
import ru.glyph.folder_bottom_sheets.form.FolderFormBottomSheetInternal
import ru.glyph.folder_bottom_sheets.form.FolderFormPresenter
import ru.glyph.folder_bottom_sheets.form.FolderFormPresenterImpl
import ru.glyph.folder_bottom_sheets.move.MoveNoteBottomSheetInternal
import ru.glyph.folder_bottom_sheets.move.MoveNotePresenter
import ru.glyph.folder_bottom_sheets.move.MoveNotePresenterImpl
import ru.glyph.navigation.api.di.bottomSheet
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.navigation.api.model.BottomSheetMeta

@OptIn(KoinExperimentalAPI::class)
object FolderBottomSheetsLocalDi {

    val module = module {
        factory {
            FolderFormPresenter.Factory { onSave ->
                FolderFormPresenterImpl(
                    onSaveAccepted = onSave,
                    navigatorLazy = inject(),
                )
            }
        }
        factory {
            FolderActionsPresenter.Factory { onRename, onDelete ->
                FolderActionsPresenterImpl(
                    onRenameAccepted = onRename,
                    onDeleteAccepted = onDelete,
                    navigatorLazy = inject(),
                )
            }
        }
        factory {
            MoveNotePresenter.Factory { onMove ->
                MoveNotePresenterImpl(
                    onMoveAccepted = onMove,
                    navigatorLazy = inject(),
                )
            }
        }

        bottomSheet<BottomSheet.FolderForm>(
            meta = BottomSheetMeta(skipPartiallyExpanded = true),
        ) { bottomSheet ->
            FolderFormBottomSheetInternal(
                presenterFactory = koinInject(),
                mode = bottomSheet.mode,
                initialName = bottomSheet.initialName,
                onSave = bottomSheet.onSave,
            )
        }

        bottomSheet<BottomSheet.FolderActions>(
            meta = BottomSheetMeta(skipPartiallyExpanded = true),
        ) { bottomSheet ->
            FolderActionsBottomSheetInternal(
                presenterFactory = koinInject(),
                folder = bottomSheet.folder,
                onRename = bottomSheet.onRename,
                onDelete = bottomSheet.onDelete,
            )
        }

        bottomSheet<BottomSheet.MoveNoteToFolder>(
            meta = BottomSheetMeta(skipPartiallyExpanded = true),
        ) { bottomSheet ->
            MoveNoteBottomSheetInternal(
                presenterFactory = koinInject(),
                folders = bottomSheet.folders,
                currentFolderId = bottomSheet.currentFolderId,
                onMove = bottomSheet.onMove,
            )
        }
    }
}
