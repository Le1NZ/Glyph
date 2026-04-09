package ru.glyph.screen.profile.api.di

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module

@OptIn(KoinExperimentalAPI::class)
internal object ProfileScreenDi {

    val module = module {
        includes(ProfileScreenLocalDi.module)
    }
}
