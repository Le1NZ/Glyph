package ru.glyph.tag_bottom_sheets.di

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import ru.glyph.navigation.api.di.bottomSheet
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.navigation.api.model.BottomSheetMeta
import ru.glyph.tag_bottom_sheets.form.TagFormBottomSheetInternal
import ru.glyph.tag_bottom_sheets.selection.TagSelectionBottomSheetInternal

@OptIn(KoinExperimentalAPI::class)
object TagBottomSheetsLocalDi {

    val module = module {
        bottomSheet<BottomSheet.TagForm>(
            meta = BottomSheetMeta(skipPartiallyExpanded = true),
        ) { bottomSheet ->
            TagFormBottomSheetInternal(bottomSheet = bottomSheet)
        }

        bottomSheet<BottomSheet.TagSelection>(
            meta = BottomSheetMeta(skipPartiallyExpanded = true),
        ) { bottomSheet ->
            TagSelectionBottomSheetInternal(bottomSheet = bottomSheet)
        }
    }
}
