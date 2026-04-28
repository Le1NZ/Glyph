package ru.glyph.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import kotlinx.coroutines.flow.Flow
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.api.model.BottomSheetMeta
import ru.glyph.navigation.ui.bottom_sheet.BottomSheetScene
import ru.glyph.navigation.ui.dialog.DialogMeta
import ru.glyph.navigation.ui.dialog.DialogScene

internal class SceneStrategyImpl(
    private val hideOverlayEvent: Flow<Unit>,
    private val onOverlayHidden: () -> Unit,
) : SceneStrategy<BaseDestination> {

    override fun SceneStrategyScope<BaseDestination>.calculateScene(
        entries: List<NavEntry<BaseDestination>>,
    ): Scene<BaseDestination>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val bottomSheetMeta = lastEntry.metadata[BOTTOM_SHEET_KEY] as? BottomSheetMeta
        val dialogMeta = lastEntry.metadata[DIALOG_KEY] as? DialogMeta

        val previousEntries = entries.dropLast(1)
        val overlaidEntries = entries.dropLast(1)

        return when {
            bottomSheetMeta != null -> BottomSheetScene(
                entry = lastEntry,
                previousEntries = previousEntries,
                overlaidEntries = overlaidEntries,
                onDismissed = onOverlayHidden,
                meta = bottomSheetMeta,
                hideOverlayEvent = hideOverlayEvent,
            )

            dialogMeta != null -> DialogScene(
                previousEntries = previousEntries,
                overlaidEntries = overlaidEntries,
                onDismissed = onOverlayHidden,
                hideOverlayEvent = hideOverlayEvent,
                entry = lastEntry,
            )

            else -> null
        }
    }

    companion object {

        private const val BOTTOM_SHEET_KEY = "bottom_sheet_scene_strategy"
        private const val DIALOG_KEY = "dialog_scene_strategy"

        fun bottomSheet(meta: BottomSheetMeta): Map<String, BottomSheetMeta> {
            return mapOf(BOTTOM_SHEET_KEY to meta)
        }

        fun dialog(meta: DialogMeta): Map<String, DialogMeta> {
            return mapOf(DIALOG_KEY to meta)
        }
    }
}

@Composable
internal fun rememberSceneStrategy(
    hideOverlayEvent: Flow<Unit>,
    onOverlayHidden: () -> Unit,
): SceneStrategyImpl {
    return remember(hideOverlayEvent, onOverlayHidden) {
        SceneStrategyImpl(
            hideOverlayEvent = hideOverlayEvent,
            onOverlayHidden = onOverlayHidden,
        )
    }
}
