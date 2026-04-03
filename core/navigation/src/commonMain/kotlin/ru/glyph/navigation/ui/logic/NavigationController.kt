package ru.glyph.navigation.ui.logic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import ru.glyph.navigation.ui.NavigatorScreenPresenter
import ru.glyph.navigation.ui.event.NavigatorEventProcessor
import ru.glyph.navigation.ui.event.rememberNavigatorEventProcessor
import ru.glyph.navigation.ui.state.NavigationState
import ru.glyph.utils.flow.collectIn

internal class NavigationController(
    private val scope: CoroutineScope,
    private val presenter: NavigatorScreenPresenter,
    private val eventProcessor: NavigatorEventProcessor,
) {
    val hideOverlayEvent = eventProcessor.hideOverlayEvent

    init {
        presenter.events.collectIn(
            scope = scope,
            collector = eventProcessor::process,
        )
    }

    fun onOverlayHidden() {
        eventProcessor.onOverlayHidden()
    }

    fun onBackFromScreenDestination() {
        presenter.onBackFromScreenDestination()
    }
}

@Composable
internal fun rememberNavigationController(
    presenter: NavigatorScreenPresenter,
    state: NavigationState,
): NavigationController {
    val scope = rememberCoroutineScope()
    val eventProcessor = rememberNavigatorEventProcessor(state)

    return remember(presenter, scope, eventProcessor) {
        NavigationController(
            scope = scope,
            presenter = presenter,
            eventProcessor = eventProcessor,
        )
    }
}
