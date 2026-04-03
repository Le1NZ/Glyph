package ru.glyph.navigation

import ru.glyph.navigation.api.deps.InitialDestinationsProvider
import ru.glyph.navigation.api.deps.NavigationDependencies

internal class NavigationDependenciesImpl(
    override val initialDestinationsProvider: InitialDestinationsProvider
) : NavigationDependencies
