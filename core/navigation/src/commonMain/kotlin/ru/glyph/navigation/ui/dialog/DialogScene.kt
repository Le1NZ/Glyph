package ru.glyph.navigation.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import kotlinx.coroutines.flow.Flow
import ru.glyph.navigation.api.dialog.BaseDialog
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.api.model.Dialog

@Immutable
internal data class DialogMeta(
    val destination: Dialog,
    val properties: DialogProperties,
)

@OptIn(ExperimentalMaterial3Api::class)
internal class DialogScene<T : BaseDestination>(
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val hideOverlayEvent: Flow<Unit>,
    private val onDismissed: () -> Unit,
    private val entry: NavEntry<T>,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)
    override val key = entry.contentKey

    override val content: @Composable (() -> Unit) = {
        key(key) {
            LaunchedEffect(hideOverlayEvent) {
                hideOverlayEvent
                    .collect { onDismissed() }
            }

            Dialog(
                onDismissRequest = onDismissed,
            ) {
                entry.Content()
            }
        }
    }
}

internal fun BaseDialog.toDialogMeta(destination: Dialog): DialogMeta {
    return DialogMeta(
        destination = destination,
        properties = properties,
    )
}
