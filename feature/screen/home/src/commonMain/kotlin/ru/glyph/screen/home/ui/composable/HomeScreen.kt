package ru.glyph.screen.home.ui.composable

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.glyph.design.Res
import ru.glyph.design.ic_add
import ru.glyph.design.ic_person
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.padding.localPaddingValues
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.home.ui.HomeScreenPresenter
import ru.glyph.screen.home.ui.HomeScreenPresenterImpl
import ru.glyph.screen.home.ui.HomeScreenPresenterPreview
import ru.glyph.screen.home.ui.HomeScreenViewModel
import ru.glyph.screen.home.ui.composable.component.HomeFoldersGrid
import ru.glyph.screen.home.ui.composable.component.HomeNoteItem
import ru.glyph.screen.home.ui.composable.component.SearchBar
import ru.glyph.string.resources.Res as StringRes
import ru.glyph.string.resources.home_create_note_cd
import ru.glyph.string.resources.home_folders_section
import ru.glyph.string.resources.home_profile_cd
import ru.glyph.string.resources.home_recent_section

@Composable
internal fun HomeScreen(
    viewModel: HomeScreenViewModel,
) {
    val presenter = remember(viewModel) { HomeScreenPresenterImpl(viewModel) }
    HomeScreenContent(presenter = presenter)
}

@Composable
private fun HomeScreenContent(
    presenter: HomeScreenPresenter,
    modifier: Modifier = Modifier,
) {
    val colors = GlyphTheme.colors
    val typography = GlyphTheme.typography
    val topPadding = localPaddingValues.calculateTopPadding()
    val bottomPadding = localPaddingValues.calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(top = topPadding)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppLogo(typography = typography, colors = colors)
                    ProfileButton(
                        onClick = presenter::onProfileClick,
                        colors = colors,
                    )
                }
                SearchBar(
                    value = presenter.searchQuery.collectAsStateWithLifecycle().value,
                    onValueChange = presenter::onSearchQueryChanged,
                )
            }

            // ── Content ─────────────────────────────────────────────────────
            val state by presenter.state.collectAsStateWithLifecycle()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp,
                    bottom = maxOf(24.dp, bottomPadding) + 72.dp,
                ),
            ) {
                item {
                    Text(
                        text = stringResource(StringRes.string.home_folders_section),
                        style = typography.heading2.copy(color = colors.textPrimary),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HomeFoldersGrid(
                        folders = state.folders,
                        onFolderClick = presenter::onFolderClick,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Text(
                        text = stringResource(StringRes.string.home_recent_section),
                        style = typography.heading2.copy(color = colors.textPrimary),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(state.recentNotes, key = { it.id }) { note ->
                    HomeNoteItem(
                        note = note,
                        onClick = { presenter.onNoteClick(note.id) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // ── FAB ─────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = presenter::onCreateNoteClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp + bottomPadding),
            containerColor = colors.fabBackground,
            contentColor = colors.fabContent,
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
private fun AppLogo(
    typography: ru.glyph.design.theme.GlyphTypography,
    colors: ru.glyph.design.theme.GlyphColorScheme,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // TODO: Replace with actual Glyph brand icon asset
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(GlyphShape.button)
                .background(color = colors.textPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "G",
                style = typography.body.copy(
                    color = colors.contentOnAccent,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Text(
            text = "Glyph",
            style = typography.heading1.copy(color = colors.textPrimary),
        )
    }
}

@Composable
private fun ProfileButton(
    onClick: () -> Unit,
    colors: ru.glyph.design.theme.GlyphColorScheme,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(GlyphShape.button)
            .background(colors.surfaceVariant),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_person),
            contentDescription = stringResource(StringRes.string.home_profile_cd),
            tint = colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
@Preview
private fun HomeScreenPreview() {
    GlyphTheme {
        HomeScreenContent(presenter = HomeScreenPresenterPreview())
    }
}
