package ru.glyph.folder_bottom_sheets.move

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res as DesignRes
import ru.glyph.design.ic_folder
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.design.theme.toGlyphColor
import ru.glyph.model.Folder
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.move_note_no_folder
import ru.glyph.string.resources.move_note_title

@Composable
internal fun MoveNoteBottomSheetContent(
    presenter: MoveNotePresenter,
    folders: List<Folder>,
    currentFolderId: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
    ) {
        Text(
            text = stringResource(Res.string.move_note_title),
            style = GlyphTheme.typography.heading1,
            color = GlyphTheme.colors.textPrimary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FolderRow(
                    name = stringResource(Res.string.move_note_no_folder),
                    iconColor = GlyphTheme.colors.textSubtle,
                    selected = currentFolderId == null,
                    onClick = { presenter.onSelect(null) },
                )
            }
            items(folders, key = { it.id }) { folder ->
                FolderRow(
                    name = folder.name,
                    iconColor = folder.color.toGlyphColor(),
                    selected = currentFolderId == folder.id,
                    onClick = { presenter.onSelect(folder.id) },
                )
            }
        }
    }
}

@Composable
private fun FolderRow(
    name: String,
    iconColor: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) GlyphTheme.colors.surfaceVariant else GlyphTheme.colors.surface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(GlyphShape.card)
            .background(color = backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = iconColor, shape = GlyphShape.iconContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignRes.drawable.ic_folder),
                contentDescription = null,
                tint = GlyphTheme.colors.contentOnAccent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = name,
            style = GlyphTheme.typography.heading3.copy(color = GlyphTheme.colors.textPrimary),
        )
    }
}
