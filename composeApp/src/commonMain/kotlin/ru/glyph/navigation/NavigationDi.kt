package ru.glyph.navigation

import org.koin.dsl.module
import ru.glyph.navigation.api.deps.InitialDestinationsProvider
import ru.glyph.navigation.api.deps.NavigationDependencies
import ru.glyph.navigation.api.di.NavigationLocalDi

internal object NavigationDi {

    val module = module {
        includes(NavigationLocalDi.module)
        factory<InitialDestinationsProvider> { InitialDestinationsProviderImpl(userCenterLazy = inject()) }
        single<NavigationDependencies> { NavigationDependenciesImpl(initialDestinationsProvider = get()) }
    }
}