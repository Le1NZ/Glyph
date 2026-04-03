package ru.glyph.home

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.glyph.navigation.api.model.Screen

@OptIn(KoinExperimentalAPI::class)
internal object HomeDi {

    val module = module {
        navigation<Screen.Home> { HomeScreen() }
    }
}