package ru.glyph.navigation

import ru.glyph.navigation.api.deps.InitialDestinationsProvider
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.api.model.Screen

internal class InitialDestinationsProviderImpl : InitialDestinationsProvider {

    override fun initialDestinations(): List<BaseDestination> {
        return listOf(Screen.Home)
    }
}