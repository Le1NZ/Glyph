package ru.glyph.share_bottom_sheet.impl

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res
import ru.glyph.design.ic_delete
import ru.glyph.design.theme.GlyphFolderColors
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.model.NotePermission
import ru.glyph.string.resources.share_bottom_sheet_add_button
import ru.glyph.string.resources.share_bottom_sheet_delete_cd
import ru.glyph.string.resources.share_bottom_sheet_email_placeholder
import ru.glyph.string.resources.share_bottom_sheet_empty_state
import ru.glyph.string.resources.share_bottom_sheet_permission_read
import ru.glyph.string.resources.share_bottom_sheet_permission_write
import ru.glyph.string.resources.share_bottom_sheet_title
import ru.glyph.string.resources.Res as StringRes

@Composable
internal fun ShareNoteBottomSheetInternal(
    presenter: ShareNotePresenter,
    modifier: Modifier = Modifier,
) {
    val state by presenter.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(StringRes.string.share_bottom_sheet_title),
            style = GlyphTheme.typography.heading1.copy(color = GlyphTheme.colors.textPrimary),
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(GlyphShape.button)
                    .background(GlyphTheme.colors.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = state.emailInput,
                    onValueChange = presenter::onEmailChanged,
                    textStyle = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textPrimary),
                    cursorBrush = SolidColor(GlyphTheme.colors.accent),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (state.emailInput.isEmpty()) {
                            Text(
                                text = stringResource(StringRes.string.share_bottom_sheet_email_placeholder),
                                style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Box(
                modifier = Modifier
                    .clip(GlyphShape.button)
                    .background(if (state.emailInput.isNotBlank() && !state.isAdding) GlyphTheme.colors.accent else GlyphTheme.colors.surfaceVariant)
                    .clickable(enabled = state.emailInput.isNotBlank() && !state.isAdding, onClick = presenter::onAddShare)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = GlyphTheme.colors.contentOnAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(StringRes.string.share_bottom_sheet_add_button),
                        style = GlyphTheme.typography.body.copy(
                            color = if (state.emailInput.isNotBlank()) GlyphTheme.colors.contentOnAccent else GlyphTheme.colors.textSecondary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.error == true -> {
                ru.glyph.design.components.ErrorBlock(
                    onRetryClick = presenter::onRetry,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                )
            }
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GlyphTheme.colors.accent)
                }
            }
            state.shares.isEmpty() -> {
                Text(
                    text = stringResource(StringRes.string.share_bottom_sheet_empty_state),
                    style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.shares, key = { it.email }) { share ->
                        ShareItemRow(
                            share = share,
                            onPermissionClick = { 
                                val newPerm = if (share.permission == NotePermission.READ) NotePermission.WRITE else NotePermission.READ
                                presenter.onUpdatePermission(share.email, newPerm)
                            },
                            onRemoveClick = { presenter.onRemoveShare(share.email) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareItemRow(
    share: NoteShareDto,
    onPermissionClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = share.email,
            style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textPrimary),
            modifier = Modifier.weight(1f)
        )
        
        Box(
            modifier = Modifier
                .clip(GlyphShape.button)
                .background(GlyphTheme.colors.surfaceVariant)
                .clickable(onClick = onPermissionClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (share.permission == NotePermission.READ) 
                    stringResource(StringRes.string.share_bottom_sheet_permission_read) 
                else 
                    stringResource(StringRes.string.share_bottom_sheet_permission_write),
                style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textPrimary)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(onClick = onRemoveClick, modifier = Modifier.size(32.dp)) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = stringResource(StringRes.string.share_bottom_sheet_delete_cd),
                tint = GlyphFolderColors.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}