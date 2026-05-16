package ru.glyph.share_bottom_sheet.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.share_bottom_sheet.impl.ShareNoteBottomSheetInternal
import ru.glyph.share_bottom_sheet.impl.ShareNotePresenter
import ru.glyph.share_bottom_sheet.impl.ShareNoteViewModel

@Composable
fun ShareNoteBottomSheet(
    overlay: BottomSheet.ShareNote,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<ShareNoteViewModel>(parameters = { parametersOf(overlay.noteId) })
    val factory = koinInject<ShareNotePresenter.Factory>()
    val presenter = remember(viewModel) { factory.create(viewModel) }

    ShareNoteBottomSheetInternal(
        presenter = presenter,
        modifier = modifier,
    )
}