package ru.glyph.navigation.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastFilterNotNull
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.api.model.OverlayDestination
import ru.glyph.navigation.api.model.Screen
import ru.glyph.navigation.ui.NavigatorScreenPresenter

@Immutable
internal class NavigationState(
    val backStack: NavBackStack<Screen>,
    overlayState: MutableState<OverlayDestination?>,
) {
    var overlay by overlayState

    val stack: List<BaseDestination> get() = (backStack + overlay).fastFilterNotNull()
}

@Composable
internal fun rememberNavigationState(
    presenter: NavigatorScreenPresenter,
): NavigationState {
    val initials = rememberInitialNavigationInfo(presenter)

    val overlay = retain<MutableState<OverlayDestination?>> {
        mutableStateOf(null)
    }

    val backStack = rememberSerializable(
        serializer = NavBackStackSerializer(),
    ) {
        NavBackStack(*initials.screens.toTypedArray())
    }

    return remember(presenter, backStack, overlay) {
        NavigationState(
            backStack = backStack,
            overlayState = overlay,
        )
    }
}
