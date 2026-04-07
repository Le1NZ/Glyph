package ru.glyph.screen.home.ui.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme

@Composable
internal fun HomeNoteTag(
    tag: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(color = GlyphTheme.colors.surfaceVariant, shape = GlyphShape.tag)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = tag,
            style = GlyphTheme.typography.caption.copy(color = GlyphTheme.colors.textSubtle),
        )
    }
}
