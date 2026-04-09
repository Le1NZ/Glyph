package ru.glyph.screen.profile.api.di

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.profile.ui.ProfileScreenViewModel
import ru.glyph.screen.profile.ui.composable.ProfileScreen

@OptIn(KoinExperimentalAPI::class)
object ProfileScreenLocalDi {

    val module = module {
        factory { ProfileScreenViewModel(get(), get(), get()) }
        navigation<Screen.Profile> { ProfileScreen(viewModel = koinViewModel<ProfileScreenViewModel>()) }
    }
}
