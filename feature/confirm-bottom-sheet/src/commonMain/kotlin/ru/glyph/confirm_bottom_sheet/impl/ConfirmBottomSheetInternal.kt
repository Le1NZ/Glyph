package ru.glyph.confirm_bottom_sheet.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal fun ConfirmBottomSheetInternal(
    presenterFactory: ConfirmBottomPresenter.Factory,
    text: @Composable () -> String,
    onConfirm: () -> Unit,
) {
    val presenter = remember(onConfirm) {
        presenterFactory.create(onConfirm)
    }

    ConfirmBottomSheetContent(
        presenter = presenter,
        text = text(),
    )
}
