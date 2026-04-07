package ru.glyph.screen.home.ui.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import ru.glyph.design.ic_folder
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import ru.glyph.design.theme.GlyphElevation
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.home.ui.composable.model.FolderUiModel
import ru.glyph.string.resources.Res as StringRes
import ru.glyph.string.resources.home_notes_count

@Composable
internal fun HomeFolderCard(
    folder: FolderUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GlyphTheme.colors
    val typography = GlyphTheme.typography

    Column(
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
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(
                    elevation = GlyphElevation.iconContainer,
                    shape = GlyphShape.iconContainer,
                    spotColor = colors.shadow,
                    ambientColor = colors.shadow,
                )
                .background(color = folder.color, shape = GlyphShape.iconContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_folder),
                contentDescription = null,
                tint = colors.contentOnAccent,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = folder.name,
            style = typography.heading3.copy(color = colors.textPrimary),
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = pluralStringResource(StringRes.plurals.home_notes_count, folder.noteCount, folder.noteCount),
            style = typography.body.copy(color = colors.textSecondary),
        )
    }
}
