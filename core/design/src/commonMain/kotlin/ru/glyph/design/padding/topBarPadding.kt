package ru.glyph.design.padding

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.glyph.design.modifier.thenIf

@Composable
fun Modifier.topBarPadding(
    withAdditional: Boolean = true,
): Modifier {
    val windowInsets = WindowInsets.safeDrawing.only(
        sides = WindowInsetsSides.Horizontal + WindowInsetsSides.Top,
    )

    return this
        .thenIf(withAdditional) {
            Modifier.padding(vertical = 8.dp)
        }
        .windowInsetsPadding(insets = windowInsets)
}
