package ru.glyph

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.di.AppDi
import ru.glyph.navigation.api.NavigatorScreen

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = { modules(AppDi.modules) },
        ),
    ) {
        GlyphTheme {
            NavigatorScreen()
        }
    }
}
