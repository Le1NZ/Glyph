package ru.glyph.navigation.ui

import androidx.lifecycle.ViewModel
import ru.glyph.navigation.api.deps.InitialDestinationsProvider
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.impl.NavigatorImpl

internal class NavigatorScreenViewModel(
    private val initialDestinationsProvider: InitialDestinationsProvider,
    private val navigatorImpl: NavigatorImpl,
) : ViewModel() {

    val navigationEvents = navigatorImpl.events

    fun onBackFromScreenDestination() {
        navigatorImpl.popBackStack()
    }

    fun getInitialDestinations(): List<BaseDestination> {
        return initialDestinationsProvider.initialDestinations()
    }
}
