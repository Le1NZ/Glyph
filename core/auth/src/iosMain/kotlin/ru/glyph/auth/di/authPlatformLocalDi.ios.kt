package ru.glyph.auth.di

import org.koin.dsl.module
import ru.glyph.auth.internal.auth.PlatformAuthProvider
import ru.glyph.auth.internal.auth.YandexIosAuthProvider

internal actual fun authPlatformModule() = module {
    single<PlatformAuthProvider> { YandexIosAuthProvider() }
}