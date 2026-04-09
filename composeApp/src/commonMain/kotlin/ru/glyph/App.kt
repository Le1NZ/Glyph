package ru.glyph

import androidx.compose.runtime.Composable
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.navigation.api.NavigatorScreen

@Composable
expect fun App()

@Composable
internal fun AppContent() {
    GlyphTheme {
        PlatformAuthEffect()
        NavigatorScreen()
    }
}

@Composable
internal expect fun PlatformAuthEffect()
