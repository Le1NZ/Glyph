package ru.glyph.tag_bottom_sheets.form

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.glyph.database.api.TagsRepository
import ru.glyph.model.FolderColor
import ru.glyph.model.Tag
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.utils.clock.currentTimeDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class TagFormPresenter(
    private val navigator: Navigator,
    private val tagsRepository: TagsRepository,
    private val bottomSheet: BottomSheet.TagForm,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalUuidApi::class)
    fun onSave(name: String, color: FolderColor) {
        scope.launch {
            val tag = Tag(
                id = Uuid.random().toString(),
                name = name,
                color = color,
                createdAt = currentTimeDuration().inWholeMilliseconds,
                updatedAt = currentTimeDuration().inWholeMilliseconds,
            )
            tagsRepository.upsert(tag)
            bottomSheet.onSave(name, color)
            navigator.hideOverlay()
        }
    }

    fun onCancel() {
        navigator.hideOverlay()
    }
}
