package ru.glyph.tag_bottom_sheets.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import ru.glyph.database.api.TagsRepository
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BottomSheet

@Composable
internal fun TagSelectionBottomSheetInternal(
    bottomSheet: BottomSheet.TagSelection,
) {
    val navigator = koinInject<Navigator>()
    val tagsRepository = koinInject<TagsRepository>()
    
    val presenter = remember(bottomSheet) {
        TagSelectionPresenter(
            navigator = navigator,
            tagsRepository = tagsRepository,
            bottomSheet = bottomSheet,
        )
    }

    val tags by presenter.tags.collectAsState(initial = emptyList())
    val selectedTagIds by presenter.selectedTagIds.collectAsState()

    TagSelectionBottomSheetContent(
        presenter = presenter,
        tags = tags,
        selectedTagIds = selectedTagIds,
    )
}
