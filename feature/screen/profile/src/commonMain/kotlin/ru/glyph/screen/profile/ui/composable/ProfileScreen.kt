package ru.glyph.screen.profile.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res
import ru.glyph.design.components.ErrorScreen
import ru.glyph.design.components.LoadingScreen
import ru.glyph.design.components.NetworkImage
import ru.glyph.design.components.PrimaryButton
import ru.glyph.design.components.ScaffoldScreen
import ru.glyph.design.ic_arrow_back
import ru.glyph.design.padding.localPaddingValues
import ru.glyph.design.padding.topBarPadding
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.profile.ui.ProfileScreenViewModel
import ru.glyph.screen.profile.ui.state.ProfileUiState
import ru.glyph.screen.profile.ui.state.UserUiModel
import ru.glyph.string.resources.profile_sign_out
import ru.glyph.string.resources.profile_title
import ru.glyph.string.resources.Res as StringRes

@Composable
internal fun ProfileScreen(
    viewModel: ProfileScreenViewModel,
    modifier: Modifier = Modifier,
) {
    ScaffoldScreen(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlyphTheme.colors.surface)
                    .topBarPadding()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IconButton(
                    onClick = viewModel::onBackClick,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = null,
                        tint = GlyphTheme.colors.textPrimary,
                    )
                }

                Text(
                    text = stringResource(StringRes.string.profile_title),
                    style = GlyphTheme.typography.heading2.copy(color = GlyphTheme.colors.textPrimary),
                )
            }
        },
    ) {
        when (val state = viewModel.uiState.collectAsStateWithLifecycle().value) {
            is ProfileUiState.Loading -> LoadingScreen()

            is ProfileUiState.Error -> ErrorScreen(
                onRetryClick = viewModel::onRetryClick,
            )

            is ProfileUiState.Success -> ProfileScreenContent(
                state = state,
                onSignOutClick = viewModel::onSignOutClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ProfileScreenContent(
    state: ProfileUiState.Success,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(localPaddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Avatar(user = state.user)

        Spacer(modifier = Modifier.height(16.dp))

        val user = state.user
        Text(
            text = user.displayName,
            style = GlyphTheme.typography.heading1.copy(color = GlyphTheme.colors.textPrimary),
            textAlign = TextAlign.Center,
        )

        if (user.email != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = stringResource(StringRes.string.profile_sign_out),
            onClick = onSignOutClick,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun Avatar(
    user: UserUiModel,
    modifier: Modifier = Modifier,
) {
    val initials = user.initials
    NetworkImage(
        url = user.avatarUrl,
        contentDescription = null,
        modifier = modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(GlyphTheme.colors.shimmer),
        error = { InitialsCircle(initials = initials) },
    )
}

@Composable
private fun InitialsCircle(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlyphTheme.colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials.uppercase(),
            style = GlyphTheme.typography.heading1.copy(
                color = GlyphTheme.colors.contentOnAccent,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
