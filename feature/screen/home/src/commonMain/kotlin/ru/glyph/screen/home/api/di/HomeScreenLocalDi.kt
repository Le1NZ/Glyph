package ru.glyph.screen.home.api.di

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.home.ui.HomeScreenViewModel
import ru.glyph.screen.home.ui.composable.HomeScreen

@OptIn(KoinExperimentalAPI::class)
object HomeScreenLocalDi {

    val module = module {
        factory { HomeScreenViewModel(get(), get()) }
        navigation<Screen.Home> { HomeScreen(viewModel = koinViewModel<HomeScreenViewModel>()) }
    }
}
