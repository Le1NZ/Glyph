package ru.glyph.navigation.api

import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.api.model.OverlayDestination
import ru.glyph.navigation.api.model.Screen

interface Navigator {

    fun navigateTo(
        screen: Screen,
        popUpTo: Screen? = null,
        popInclusive: Boolean = false,
        clearBackStack: Boolean = false,
    )

    fun navigateTo(
        destinations: List<BaseDestination>,
        popUpTo: Screen? = null,
        popInclusive: Boolean = false,
        restoreState: Boolean = false,
    )

    fun showOverlay(
        overlay: OverlayDestination,
    )

    fun hideOverlay()

    fun popBackStack(
        screen: Screen? = null,
        inclusive: Boolean = false,
        saveState: Boolean = false,
    )

    fun removeFromBackStack(
        screen: Screen,
    )
}
