package ru.glyph.navigation.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.api.model.OverlayDestination
import ru.glyph.navigation.api.model.Screen
import ru.glyph.navigation.ui.event.NavigatorEvent

internal class NavigatorImpl(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : Navigator {

    private val _events = MutableSharedFlow<NavigatorEvent>()
    val events = _events.asSharedFlow()

    override fun navigateTo(
        screen: Screen,
        popUpTo: Screen?,
        popInclusive: Boolean,
        clearBackStack: Boolean,
    ) {
        emitEvent {
            NavigatorEvent.NavigateTo(
                screens = listOf(screen),
                popUpTo = popUpTo,
                popInclusive = popInclusive,
                clearBackStack = clearBackStack,
            )
        }
    }

    override fun navigateTo(
        destinations: List<BaseDestination>,
        popUpTo: Screen?,
        popInclusive: Boolean,
        restoreState: Boolean,
    ) {
        val screens = destinations.filterIsInstance<Screen>()
        if (screens.isNotEmpty()) {
            emitEvent {
                NavigatorEvent.NavigateTo(
                    screens = screens,
                    popUpTo = popUpTo,
                    popInclusive = popInclusive,
                    clearBackStack = restoreState,
                )
            }
        }

        destinations.filterIsInstance<OverlayDestination>().forEach { overlay ->
            showOverlay(overlay)
        }
    }

    override fun showOverlay(overlay: OverlayDestination) {
        emitEvent {
            NavigatorEvent.ShowOverlay(overlay)
        }
    }

    override fun hideOverlay() {
        emitEvent { NavigatorEvent.HideOverlay }
    }

    override fun popBackStack(
        screen: Screen?,
        inclusive: Boolean,
        saveState: Boolean
    ) {
        emitEvent {
            NavigatorEvent.PopBackStack(
                screen = screen,
                inclusive = inclusive,
                saveState = saveState,
            )
        }
    }

    override fun removeFromBackStack(
        screen: Screen,
    ) {
        emitEvent {
            NavigatorEvent.RemoveFromBackStack(screen = screen)
        }
    }

    private fun emitEvent(eventBuilder: suspend () -> NavigatorEvent) {
        scope.launch { _events.emit(eventBuilder()) }
    }
}
