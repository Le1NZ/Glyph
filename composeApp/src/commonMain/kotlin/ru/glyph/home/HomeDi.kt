package ru.glyph.home

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import ru.glyph.screen.home.api.di.HomeScreenLocalDi

@OptIn(KoinExperimentalAPI::class)
internal object HomeDi {

    val module = module {
        includes(HomeScreenLocalDi.module)
    }
}