package ru.glyph

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import ru.glyph.di.AppDi

@Composable
actual fun App() {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = { modules(AppDi.modules) },
        ),
    ) {
        AppContent()
    }
}

@Composable
internal actual fun PlatformAuthEffect() {
    // No-op: iOS auth is handled directly in YandexIosAuthProvider via ASWebAuthenticationSession
}
