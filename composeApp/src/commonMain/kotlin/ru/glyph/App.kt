package ru.glyph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.filter
import org.koin.compose.koinInject
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.UserState
import ru.glyph.database.api.NotesRepository
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.navigation.api.NavigatorScreen

@Composable
expect fun App()

@Composable
internal fun AppContent() {
    DatabaseCleanupEffect()
    GlyphTheme {
        PlatformAuthEffect()
        NavigatorScreen()
    }
}

/** Очищает локальную БД при выходе из аккаунта */
@Composable
private fun DatabaseCleanupEffect() {
    val userCenter: UserCenter = koinInject()
    val notesRepository: NotesRepository = koinInject()
    LaunchedEffect(Unit) {
        userCenter.authState
            .filter { it == UserState.NotAuthorized }
            .collect { notesRepository.deleteAll() }
    }
}

@Composable
internal expect fun PlatformAuthEffect()
