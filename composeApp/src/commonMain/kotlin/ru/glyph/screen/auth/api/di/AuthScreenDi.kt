package ru.glyph.screen.auth.api.di

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module

@OptIn(KoinExperimentalAPI::class)
internal object AuthScreenDi {

    val module = module {
        includes(AuthScreenLocalDi.module)
    }
}
