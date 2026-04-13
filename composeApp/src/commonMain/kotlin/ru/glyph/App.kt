package ru.glyph

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import org.koin.compose.viewmodel.koinViewModel
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.navigation.api.NavigatorScreen

@Composable
expect fun App()

@Composable
internal fun AppContent() {
    val viewModel: AppViewModel = koinViewModel()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onForeground()
    }
    GlyphTheme {
        PlatformAuthEffect()
        NavigatorScreen()
    }
}

@Composable
internal expect fun PlatformAuthEffect()
