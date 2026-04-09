package ru.glyph.auth.di

import org.koin.dsl.binds
import org.koin.dsl.module
import ru.glyph.auth.api.AndroidAuthLauncher
import ru.glyph.auth.internal.auth.PlatformAuthProvider
import ru.glyph.auth.internal.auth.YandexAndroidAuthProvider

internal actual fun authPlatformModule() = module {
    single { YandexAndroidAuthProvider() } binds arrayOf(
        PlatformAuthProvider::class,
        AndroidAuthLauncher::class,
    )
}
