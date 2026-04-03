package ru.glyph.navigation.ui.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.util.fastCoerceAtMost
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import ru.glyph.navigation.api.model.Screen
import ru.glyph.navigation.ui.state.NavigationState

internal class NavigatorEventProcessor(
    private val state: NavigationState,
) {

    private val _hideOverlayEvent = MutableSharedFlow<Unit>()
    val hideOverlayEvent = _hideOverlayEvent.asSharedFlow()

    suspend fun process(
        event: NavigatorEvent,
    ) {
        when (event) {
            is NavigatorEvent.HideOverlay -> process(event)
            is NavigatorEvent.NavigateTo -> process(event)
            is NavigatorEvent.ShowOverlay -> process(event)
            is NavigatorEvent.PopBackStack -> process(event)
            is NavigatorEvent.RemoveFromBackStack -> process(event)
        }
    }

    fun onOverlayHidden() {
        state.overlay = null
    }

    private fun process(
        event: NavigatorEvent.NavigateTo,
    ) {
        event.popUpTo?.let { screen ->
            val index = state.backStack.indexOf(screen)
            val size = state.backStack.size
            when {
                index == -1 -> Unit
                event.popInclusive -> state.backStack.subList(index, size).clear()
                else -> state.backStack.subList(index + 1, size).clear()
            }
        }

        if (event.clearBackStack) {
            state.backStack.clear()
        }
        state.backStack.addAll(event.screens)
    }

    private fun process(
        event: NavigatorEvent.PopBackStack,
    ) {
        when {
            event.screen == null -> state.backStack.removeLastOrNull()
            else -> popUpTo(
                screen = event.screen,
                inclusive = event.inclusive
            )
        }
    }

    private fun process(
        event: NavigatorEvent.RemoveFromBackStack,
    ) {
        if (state.backStack.size <= 1) return

        state.backStack.remove(event.screen)
    }

    private suspend fun process(
        event: NavigatorEvent.ShowOverlay,
    ) {
        _hideOverlayEvent.emit(Unit)
        awaitCloseOverlay()
        state.overlay = event.overlay
    }

    private suspend fun process(
        event: NavigatorEvent.HideOverlay,
    ) {
        _hideOverlayEvent.emit(Unit)
    }

    private fun popUpTo(
        screen: Screen,
        inclusive: Boolean,
    ) {
        val lastIndex = state.backStack.lastIndexOf(screen)
        if (lastIndex == -1) {
            error("No screen $screen in stack")
        } else {
            val indexToClear = if (inclusive) {
                lastIndex
            } else {
                lastIndex + 1
            }.fastCoerceAtMost(state.backStack.lastIndex)

            state.backStack.subList(indexToClear, state.backStack.size).clear()
        }
    }

    private suspend fun awaitCloseOverlay() {
        snapshotFlow { state.overlay }.firstOrNull { it == null }
    }
}

@Composable
internal fun rememberNavigatorEventProcessor(
    state: NavigationState,
): NavigatorEventProcessor {
    return remember(state) {
        NavigatorEventProcessor(
            state = state,
        )
    }
}
