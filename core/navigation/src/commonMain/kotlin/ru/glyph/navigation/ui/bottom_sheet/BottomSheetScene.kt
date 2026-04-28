package ru.glyph.navigation.ui.bottom_sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import kotlinx.coroutines.flow.Flow
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.api.model.BottomSheetMeta

@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetScene<T : BaseDestination>(
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val hideOverlayEvent: Flow<Unit>,
    private val onDismissed: () -> Unit,
    private val meta: BottomSheetMeta,
    private val entry: NavEntry<T>,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)
    override val key = entry.contentKey

    override val content: @Composable (() -> Unit) = {
        key(key) {
            val state = rememberModalBottomSheetState(
                skipPartiallyExpanded = meta.skipPartiallyExpanded,
            )

            LaunchedEffect(hideOverlayEvent) {
                hideOverlayEvent.collect {
                    state.hide()
                    onDismissed()
                }
            }

            ModalBottomSheet(
                sheetState = state,
                onDismissRequest = onDismissed,
            ) {
                entry.Content()
            }
        }
    }
}
