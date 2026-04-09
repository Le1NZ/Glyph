package ru.glyph.screen.profile.ui.composable

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import ru.glyph.design.components.NetworkImage
import ru.glyph.design.ic_arrow_back
import ru.glyph.design.padding.localPaddingValues
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.profile.ui.ProfileScreenViewModel
import ru.glyph.screen.profile.ui.state.ProfileUiState
import ru.glyph.screen.profile.ui.state.UserUiModel
import ru.glyph.string.resources.Res as StringRes
import ru.glyph.string.resources.profile_sign_out
import ru.glyph.string.resources.profile_title

@Composable
internal fun ProfileScreen(
    viewModel: ProfileScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreenContent(
        uiState = uiState,
        onSignOutClick = viewModel::onSignOutClick,
        onBackClick = viewModel::onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GlyphTheme.colors.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlyphTheme.colors.surface)
                .padding(top = localPaddingValues.calculateTopPadding())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = GlyphTheme.colors.textPrimary,
                )
            }
            Text(
                text = stringResource(StringRes.string.profile_title),
                style = GlyphTheme.typography.heading2.copy(color = GlyphTheme.colors.textPrimary),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = localPaddingValues.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            val user = (uiState as? ProfileUiState.Success)?.user

            Avatar(user = user)

            Spacer(modifier = Modifier.height(16.dp))

            if (user != null) {
                Text(
                    text = user.displayName,
                    style = GlyphTheme.typography.heading1.copy(color = GlyphTheme.colors.textPrimary),
                    textAlign = TextAlign.Center,
                )
                if (!user.email.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSignOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = GlyphShape.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlyphTheme.colors.surfaceVariant,
                    contentColor = GlyphTheme.colors.textPrimary,
                ),
            ) {
                Text(
                    text = stringResource(StringRes.string.profile_sign_out),
                    style = GlyphTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Avatar(
    user: UserUiModel?,
    modifier: Modifier = Modifier,
) {
    val initials = user?.initials ?: "?"
    NetworkImage(
        url = user?.avatarUrl,
        contentDescription = null,
        modifier = modifier
            .size(88.dp)
            .clip(CircleShape),
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
