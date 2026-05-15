package ru.glyph.tag_bottom_sheets.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import ru.glyph.database.api.TagsRepository
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BottomSheet

@Composable
internal fun TagFormBottomSheetInternal(
    bottomSheet: BottomSheet.TagForm,
) {
    val navigator = koinInject<Navigator>()
    val tagsRepository = koinInject<TagsRepository>()

    val presenter = remember(bottomSheet) {
        TagFormPresenter(
            navigator = navigator,
            tagsRepository = tagsRepository,
            bottomSheet = bottomSheet,
        )
    }

    TagFormBottomSheetContent(
        presenter = presenter,
        mode = bottomSheet.mode,
        initialName = bottomSheet.initialName,
        initialColor = bottomSheet.initialColor,
    )
}
