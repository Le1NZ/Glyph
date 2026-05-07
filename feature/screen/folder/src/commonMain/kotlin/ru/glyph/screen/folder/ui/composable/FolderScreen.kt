package ru.glyph.screen.folder.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res
import ru.glyph.design.ic_add
import ru.glyph.design.ic_arrow_back
import ru.glyph.design.ic_description
import ru.glyph.design.ic_more_horiz
import ru.glyph.design.components.FoldersGrid
import ru.glyph.design.components.NoteCard
import ru.glyph.design.padding.localPaddingValues
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.folder.ui.FolderScreenPresenter
import ru.glyph.screen.folder.ui.FolderScreenPresenterImpl
import ru.glyph.screen.folder.ui.FolderScreenViewModel
import ru.glyph.string.resources.Res as StringRes
import ru.glyph.string.resources.folder_actions_cd
import ru.glyph.string.resources.folder_all_notes_section
import ru.glyph.string.resources.folder_create_action
import ru.glyph.string.resources.home_create_note_cd
import ru.glyph.string.resources.home_empty_subtitle
import ru.glyph.string.resources.home_folders_section
import ru.glyph.string.resources.note_back_cd

@Composable
internal fun FolderScreen(
    viewModel: FolderScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val presenter = remember(viewModel) { FolderScreenPresenterImpl(viewModel) }
    FolderScreenContent(presenter = presenter, modifier = modifier)
}

@Composable
internal fun FolderScreenContent(
    presenter: FolderScreenPresenter,
    modifier: Modifier = Modifier,
) {
    val state by presenter.state.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlyphTheme.colors.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FolderTopBar(
                title = state.folderName,
                onBackClick = presenter::onBackClick,
                onActionsClick = presenter::onCurrentFolderActionsClick,
            )

            FolderBody(
                presenter = presenter,
                modifier = Modifier.fillMaxSize(),
            )
        }

        FloatingActionButton(
            onClick = presenter::onCreateNoteClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp + localPaddingValues.calculateBottomPadding()),
            containerColor = GlyphTheme.colors.fabBackground,
            contentColor = GlyphTheme.colors.fabContent,
            shape = GlyphShape.button,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = stringResource(StringRes.string.home_create_note_cd),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun FolderBody(
    presenter: FolderScreenPresenter,
    modifier: Modifier = Modifier,
) {
    val state by presenter.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 24.dp,
            bottom = maxOf(24.dp, localPaddingValues.calculateBottomPadding()) + 72.dp,
        ),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(StringRes.string.home_folders_section),
                    style = GlyphTheme.typography.heading2.copy(color = GlyphTheme.colors.textPrimary),
                )
                CreateSubfolderButton(onClick = presenter::onCreateSubfolderClick)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.subfolders.isNotEmpty()) {
            item {
                FoldersGrid(
                    folders = state.subfolders,
                    onFolderClick = presenter::onSubfolderClick,
                    onFolderActionsClick = presenter::onSubfolderActionsClick,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        item {
            Text(
                text = stringResource(StringRes.string.folder_all_notes_section),
                style = GlyphTheme.typography.heading2.copy(color = GlyphTheme.colors.textPrimary),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.notes.isEmpty()) {
            item {
                EmptyState(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
            }
        } else {
            items(state.notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onClick = { presenter.onNoteClick(note.id) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FolderTopBar(
    title: String,
    onBackClick: () -> Unit,
    onActionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GlyphTheme.colors.surface)
            .padding(top = localPaddingValues.calculateTopPadding())
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(StringRes.string.note_back_cd),
                tint = GlyphTheme.colors.textPrimary,
            )
        }

        Text(
            text = title,
            style = GlyphTheme.typography.heading2.copy(color = GlyphTheme.colors.textPrimary),
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )

        val actionsLabel = stringResource(StringRes.string.folder_actions_cd)
        IconButton(
            onClick = onActionsClick,
            modifier = Modifier.semantics { contentDescription = actionsLabel },
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_more_horiz),
                contentDescription = null,
                tint = GlyphTheme.colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CreateSubfolderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(StringRes.string.folder_create_action)
    Row(
        modifier = modifier
            .clip(GlyphShape.button)
            .background(color = GlyphTheme.colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = null,
            tint = GlyphTheme.colors.textPrimary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textPrimary),
        )
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_description),
            contentDescription = null,
            tint = GlyphTheme.colors.textSubtle,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(StringRes.string.home_empty_subtitle),
            style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary),
            textAlign = TextAlign.Center,
        )
    }
}
