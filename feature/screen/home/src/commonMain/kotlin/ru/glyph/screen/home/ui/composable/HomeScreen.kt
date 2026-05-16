package ru.glyph.screen.home.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res
import ru.glyph.design.ic_add
import ru.glyph.design.ic_description
import ru.glyph.design.ic_person
import ru.glyph.design.ic_search
import ru.glyph.design.padding.localPaddingValues
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.home.ui.HomeScreenPresenter
import ru.glyph.screen.home.ui.HomeScreenPresenterImpl
import ru.glyph.screen.home.ui.HomeScreenPresenterPreview
import ru.glyph.screen.home.ui.HomeScreenViewModel
import ru.glyph.design.components.FoldersGrid
import ru.glyph.design.components.NoteCard
import ru.glyph.screen.home.ui.composable.component.SearchBar
import ru.glyph.screen.home.ui.composable.model.HomeUiState
import ru.glyph.string.resources.Res as StringRes
import ru.glyph.string.resources.folder_create_action
import ru.glyph.string.resources.home_create_note_cd
import ru.glyph.string.resources.home_empty_subtitle
import ru.glyph.string.resources.home_folders_section
import ru.glyph.string.resources.home_profile_cd
import ru.glyph.string.resources.home_recent_section
import ru.glyph.string.resources.home_search_no_results_subtitle
import ru.glyph.string.resources.home_search_no_results_title
import ru.glyph.string.resources.tag_selection_create_new

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import ru.glyph.design.theme.toGlyphColor
import ru.glyph.model.Tag

import ru.glyph.design.components.LoadingScreen

@Composable
internal fun HomeScreen(
    viewModel: HomeScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val presenter = remember(viewModel) { HomeScreenPresenterImpl(viewModel) }
    HomeScreenContent(presenter = presenter, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenContent(
    presenter: HomeScreenPresenter,
    modifier: Modifier = Modifier,
) {
    val state by presenter.state.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val focusManager = LocalFocusManager.current
    val backState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = backState,
        isBackEnabled = state.searchQuery.isNotBlank(),
        onBackCompleted = {
            if (isImeVisible) {
                focusManager.clearFocus()
            } else {
                presenter.onSearchQueryChanged("")
            }
        },
    )

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = presenter::onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GlyphTheme.colors.background),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeHeader(
                    presenter = presenter,
                    state = state,
                    searchQuery = presenter.searchQuery.collectAsStateWithLifecycle().value,
                )

                if (state.isLoading) {
                    LoadingScreen(modifier = Modifier.fillMaxSize())
                } else {
                    HomeBody(
                        state = state,
                        presenter = presenter,
                        focusManager = focusManager,
                    )
                }
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
}

@Composable
private fun HomeHeader(
    presenter: HomeScreenPresenter,
    state: HomeUiState,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GlyphTheme.colors.surface)
            .padding(top = localPaddingValues.calculateTopPadding())
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppLogo()
            ProfileButton(onClick = presenter::onProfileClick)
        }
        SearchBar(
            value = searchQuery,
            onValueChange = presenter::onSearchQueryChanged,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        TagFilterRow(
            availableTags = state.availableTags,
            selectedTagIds = state.selectedTagIdsForFilter,
            onTagClick = presenter::onTagFilterClick,
            onCreateTagClick = presenter::onCreateTagClick,
        )
    }
}

@Composable
private fun TagFilterRow(
    availableTags: List<Tag>,
    selectedTagIds: Set<String>,
    onTagClick: (String) -> Unit,
    onCreateTagClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .clip(GlyphShape.button)
                    .background(color = GlyphTheme.colors.surfaceVariant)
                    .clickable { onCreateTagClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = stringResource(StringRes.string.tag_selection_create_new),
                    tint = GlyphTheme.colors.textPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        items(availableTags, key = { it.id }) { tag ->
            val isSelected = selectedTagIds.contains(tag.id)
            val backgroundColor = if (isSelected) tag.color.toGlyphColor().copy(alpha = 0.15f) else GlyphTheme.colors.surfaceVariant
            val borderColor = if (isSelected) tag.color.toGlyphColor().copy(alpha = 0.5f) else Color.Transparent
            val textColor = if (isSelected) tag.color.toGlyphColor() else GlyphTheme.colors.textSecondary

            Box(
                modifier = Modifier
                    .clip(GlyphShape.button)
                    .background(color = backgroundColor)
                    .border(1.dp, borderColor, GlyphShape.button)
                    .clickable { onTagClick(tag.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = tag.name,
                    style = GlyphTheme.typography.body.copy(color = textColor),
                )
            }
        }
    }
}

@Composable
private fun HomeBody(
    state: HomeUiState,
    presenter: HomeScreenPresenter,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState, focusManager) {
        snapshotFlow { lazyListState.isScrollInProgress }
            .filter { it }
            .collect { focusManager.clearFocus() }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 24.dp,
            bottom = maxOf(24.dp, localPaddingValues.calculateBottomPadding()) + 72.dp,
        ),
    ) {
        // ── Folders section ─────────────────────────────────────────────
        if (state.searchQuery.isBlank()) {
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
                    CreateFolderButton(onClick = presenter::onCreateFolderClick)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (state.folders.isNotEmpty()) {
                item {
                    FoldersGrid(
                        folders = state.folders,
                        onFolderClick = presenter::onFolderClick,
                        onFolderActionsClick = presenter::onFolderActionsClick,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        // ── Notes section ───────────────────────────────────────────────
        item {
            Text(
                text = stringResource(StringRes.string.home_recent_section),
                style = GlyphTheme.typography.heading2.copy(color = GlyphTheme.colors.textPrimary),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.recentNotes.isEmpty()) {
            item {
                if (state.searchQuery.isNotBlank()) {
                    SearchNoResultsState(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
                } else {
                    NotesEmptyHintState(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
                }
            }
        } else {
            items(state.recentNotes, key = { it.id }) { note ->
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
private fun CreateFolderButton(
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
private fun AppLogo(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(GlyphShape.button)
                .background(color = GlyphTheme.colors.textPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "G",
                style = GlyphTheme.typography.body.copy(
                    color = GlyphTheme.colors.contentOnAccent,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Text(
            text = "Glyph",
            style = GlyphTheme.typography.heading1.copy(color = GlyphTheme.colors.textPrimary),
        )
    }
}

@Composable
private fun ProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        shape = GlyphShape.button,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = GlyphTheme.colors.surfaceVariant,
        ),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_person),
            contentDescription = stringResource(StringRes.string.home_profile_cd),
            tint = GlyphTheme.colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun NotesEmptyHintState(
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

@Composable
private fun SearchNoResultsState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_search),
            contentDescription = null,
            tint = GlyphTheme.colors.textSubtle,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(StringRes.string.home_search_no_results_title),
            style = GlyphTheme.typography.heading2.copy(color = GlyphTheme.colors.textPrimary),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(StringRes.string.home_search_no_results_subtitle),
            style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary),
            textAlign = TextAlign.Center,
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
