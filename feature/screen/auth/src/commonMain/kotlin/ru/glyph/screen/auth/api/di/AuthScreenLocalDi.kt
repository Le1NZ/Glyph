package ru.glyph.screen.auth.api.di

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.auth.ui.AuthScreenViewModel
import ru.glyph.screen.auth.ui.composable.AuthScreen

@OptIn(KoinExperimentalAPI::class)
object AuthScreenLocalDi {

    val module = module {
        factory { AuthScreenViewModel(get(), get(), get()) }
        navigation<Screen.Auth> { AuthScreen(viewModel = koinViewModel<AuthScreenViewModel>()) }
    }
}
