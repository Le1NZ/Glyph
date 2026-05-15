package ru.glyph.tag_bottom_sheets.selection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.glyph.database.api.TagsRepository
import ru.glyph.model.Tag
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BottomSheet

internal class TagSelectionPresenter(
    private val navigator: Navigator,
    private val tagsRepository: TagsRepository,
    private val bottomSheet: BottomSheet.TagSelection,
) {
    val tags: kotlinx.coroutines.flow.Flow<List<Tag>> = tagsRepository.observeAll()

    private val _selectedTagIds = MutableStateFlow(bottomSheet.selectedTagIds.toSet())
    val selectedTagIds: StateFlow<Set<String>> = _selectedTagIds.asStateFlow()

    fun onTagClick(tagId: String) {
        _selectedTagIds.update { current ->
            if (current.contains(tagId)) {
                current - tagId
            } else {
                current + tagId
            }
        }
    }

    fun onSave() {
        bottomSheet.onSave(_selectedTagIds.value.toList())
        navigator.hideOverlay()
    }

    fun onCreateTag() {
        navigator.showOverlay(
            BottomSheet.TagForm(
                mode = BottomSheet.TagForm.Mode.Create,
                onSave = { _, _ -> }
            )
        )
    }

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    fun onDeleteTag(tagId: String) {
        kotlinx.coroutines.GlobalScope.launch {
            tagsRepository.deleteById(tagId)
        }
    }
}
