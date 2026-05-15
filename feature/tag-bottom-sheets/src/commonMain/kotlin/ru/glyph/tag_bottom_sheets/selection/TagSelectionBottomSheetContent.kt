package ru.glyph.tag_bottom_sheets.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import ru.glyph.design.ic_add
import ru.glyph.design.ic_check
import ru.glyph.design.ic_delete
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.design.theme.toGlyphColor
import ru.glyph.model.Tag
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.tag_selection_title
import ru.glyph.string.resources.tag_selection_save
import ru.glyph.string.resources.tag_selection_create_new
import ru.glyph.string.resources.tag_selection_delete_cd
import ru.glyph.design.components.PrimaryButton

@Composable
internal fun TagSelectionBottomSheetContent(
    presenter: TagSelectionPresenter,
    tags: List<Tag>,
    selectedTagIds: Set<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.tag_selection_title),
                style = GlyphTheme.typography.heading1,
                color = GlyphTheme.colors.textPrimary,
            )
            
            Icon(
                painter = painterResource(DesignRes.drawable.ic_add),
                contentDescription = stringResource(Res.string.tag_selection_create_new),
                tint = GlyphTheme.colors.accent,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = presenter::onCreateTag)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tags, key = { it.id }) { tag ->
                TagRow(
                    name = tag.name,
                    iconColor = tag.color.toGlyphColor(),
                    selected = selectedTagIds.contains(tag.id),
                    onClick = { presenter.onTagClick(tag.id) },
                    onDeleteClick = { presenter.onDeleteTag(tag.id) },
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        PrimaryButton(
            text = stringResource(Res.string.tag_selection_save),
            onClick = presenter::onSave,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TagRow(
    name: String,
    iconColor: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) iconColor.copy(alpha = 0.15f) else GlyphTheme.colors.surfaceVariant
    val borderColor = if (selected) iconColor.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(GlyphShape.card)
            .background(color = backgroundColor)
            .border(1.dp, borderColor, GlyphShape.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color = iconColor, shape = GlyphShape.iconContainer),
            contentAlignment = Alignment.Center,
        ) {
            // No icon for tags, just color circle
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(iconColor)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = name,
            style = GlyphTheme.typography.heading3.copy(color = GlyphTheme.colors.textPrimary),
            modifier = Modifier.weight(1f),
        )
        
        if (selected) {
            Icon(
                painter = painterResource(DesignRes.drawable.ic_check),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        androidx.compose.material3.IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(DesignRes.drawable.ic_delete),
                contentDescription = stringResource(Res.string.tag_selection_delete_cd),
                tint = GlyphTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
