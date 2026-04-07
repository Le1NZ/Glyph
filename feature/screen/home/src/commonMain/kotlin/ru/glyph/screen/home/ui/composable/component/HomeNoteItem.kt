package ru.glyph.screen.home.ui.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import ru.glyph.design.Res
import ru.glyph.design.ic_description
import org.jetbrains.compose.resources.painterResource
import ru.glyph.design.theme.GlyphElevation
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.home.ui.composable.model.NoteUiModel

@Composable
internal fun HomeNoteItem(
    note: NoteUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GlyphTheme.colors
    val typography = GlyphTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = GlyphElevation.card,
                shape = GlyphShape.card,
                spotColor = colors.shadow,
                ambientColor = colors.shadow,
            )
            .background(color = colors.surface, shape = GlyphShape.card)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = colors.surfaceVariant, shape = GlyphShape.iconContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_description),
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = note.title,
                style = typography.heading3.copy(color = colors.textPrimary),
                maxLines = 1,
            )
            Text(
                text = note.updatedAt,
                style = typography.body.copy(color = colors.textSecondary),
                maxLines = 1,
            )
            if (note.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.tags.forEach { tag ->
                        HomeNoteTag(tag = tag)
                    }
                }
            }
        }
    }
}
