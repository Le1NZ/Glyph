package ru.glyph.navigation

import org.koin.dsl.module
import ru.glyph.navigation.api.deps.InitialDestinationsProvider
import ru.glyph.navigation.api.deps.NavigationDependencies

internal object NavigationDi {

    val module = module {
        factory<InitialDestinationsProvider> { InitialDestinationsProviderImpl() }
        single<NavigationDependencies> { NavigationDependenciesImpl(initialDestinationsProvider = get()) }
    }
}