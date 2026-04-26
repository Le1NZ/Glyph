package ru.glyph.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import ru.glyph.design.padding.localPaddingValues
import ru.glyph.design.theme.GlyphTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        show = true
    }

    Box(
        modifier = modifier
            .padding(localPaddingValues)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (show) {
            CircularProgressIndicator(
                color = GlyphTheme.colors.textPrimary,
            )
        }
    }
}
