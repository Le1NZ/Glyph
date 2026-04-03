package ru.glyph.navigation.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import ru.glyph.navigation.api.model.OverlayDestination
import ru.glyph.navigation.api.model.Screen
import ru.glyph.navigation.ui.NavigatorScreenPresenter

@Immutable
internal data class InitialNavigationInfo(
    val screens: List<Screen>,
    val overlays: List<OverlayDestination>,
)

@Composable
internal fun rememberInitialNavigationInfo(
    presenter: NavigatorScreenPresenter,
): InitialNavigationInfo {
    return remember { initialNavigationInfo(presenter) }
}

private fun initialNavigationInfo(
    presenter: NavigatorScreenPresenter,
): InitialNavigationInfo {
    val initialDestinations = presenter.getInitialDestinations()
    val initialScreens = initialDestinations.filterIsInstance<Screen>()
    val initialOverlays = initialDestinations.filterIsInstance<OverlayDestination>()

    return InitialNavigationInfo(
        screens = initialScreens,
        overlays = initialOverlays,
    )
}
