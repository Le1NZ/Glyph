package ru.glyph.confirm_bottom_sheet.di

import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import ru.glyph.confirm_bottom_sheet.impl.ConfirmBottomPresenter
import ru.glyph.confirm_bottom_sheet.impl.ConfirmBottomPresenterImpl
import ru.glyph.confirm_bottom_sheet.impl.ConfirmBottomSheetInternal
import ru.glyph.navigation.api.di.bottomSheet
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.navigation.api.model.BottomSheetMeta

@OptIn(KoinExperimentalAPI::class)
object ConfirmBottomSheetLocalDi {

    val module = module {
        factory {
            ConfirmBottomPresenter.Factory { onConfirm ->
                ConfirmBottomPresenterImpl(
                    onConfirmAccepted = onConfirm,
                    navigatorLazy = inject(),
                )
            }
        }

        bottomSheet<BottomSheet.Confirm>(
            meta = BottomSheetMeta(skipPartiallyExpanded = true),
        ) { bottomSheet ->
            ConfirmBottomSheetInternal(
                presenterFactory = koinInject(),
                text = bottomSheet.text,
                onConfirm = bottomSheet.onConfirm,
            )
        }
    }
}