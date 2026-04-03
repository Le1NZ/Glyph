package ru.glyph.navigation.api.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.impl.NavigatorImpl
import ru.glyph.navigation.ui.NavigatorScreenViewModel

object NavigationLocalDi {

    val module = module {
        single { NavigatorImpl() } bind Navigator::class
        viewModel { NavigatorScreenViewModel(navigatorImpl = get()) }
    }
}
