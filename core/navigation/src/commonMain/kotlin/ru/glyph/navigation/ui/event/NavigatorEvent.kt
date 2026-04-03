package ru.glyph.navigation.ui.event

import androidx.compose.runtime.Immutable
import ru.glyph.navigation.api.model.OverlayDestination
import ru.glyph.navigation.api.model.Screen

@Immutable
internal sealed interface NavigatorEvent {

    data class NavigateTo(
        val screens: List<Screen>,
        val popUpTo: Screen?,
        val popInclusive: Boolean,
        val clearBackStack: Boolean,
    ) : NavigatorEvent

    data class PopBackStack(
        val screen: Screen?,
        val inclusive: Boolean,
        val saveState: Boolean,
    ) : NavigatorEvent

    data class RemoveFromBackStack(
        val screen: Screen,
    ) : NavigatorEvent

    data class ShowOverlay(
        val overlay: OverlayDestination,
    ) : NavigatorEvent

    data object HideOverlay : NavigatorEvent
}
