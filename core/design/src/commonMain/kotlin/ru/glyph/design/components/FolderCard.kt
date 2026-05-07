package ru.glyph.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res
import ru.glyph.design.ic_folder
import ru.glyph.design.ic_more_horiz
import ru.glyph.design.theme.GlyphElevation
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.string.resources.Res as StringRes
import ru.glyph.string.resources.folder_actions_cd
import ru.glyph.string.resources.home_notes_count

@Immutable
data class FolderCardUiModel(
    val id: String,
    val name: String,
    val noteCount: Int,
    val color: Color,
)

@Composable
fun FolderCard(
    folder: FolderCardUiModel,
    onClick: () -> Unit,
    onActionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GlyphTheme.colors
    val typography = GlyphTheme.typography
    val actionsLabel = stringResource(StringRes.string.folder_actions_cd)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = GlyphElevation.card,
                shape = GlyphShape.card,
                spotColor = colors.shadow,
                ambientColor = colors.shadow,
            )
            .clip(GlyphShape.card)
            .background(color = colors.surface)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
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
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onActionsClick)
                    .semantics { contentDescription = actionsLabel },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_more_horiz),
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier
                        .size(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
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

@Composable
fun FoldersGrid(
    folders: List<FolderCardUiModel>,
    onFolderClick: (String) -> Unit,
    onFolderActionsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        folders.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { folder ->
                    FolderCard(
                        folder = folder,
                        onClick = { onFolderClick(folder.id) },
                        onActionsClick = { onFolderActionsClick(folder.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}