package ru.glyph.screen.auth.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.auth.ui.AuthScreenViewModel
import ru.glyph.screen.auth.ui.composable.model.AuthUiEffect
import ru.glyph.screen.auth.ui.composable.model.AuthUiState
import ru.glyph.string.resources.auth_error
import ru.glyph.string.resources.auth_sign_in_button
import ru.glyph.string.resources.auth_subtitle
import ru.glyph.string.resources.auth_title
import ru.glyph.string.resources.Res as StringRes

@Composable
internal fun AuthScreen(
    viewModel: AuthScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val errorText = stringResource(StringRes.string.auth_error)

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AuthUiEffect.ErrorMessage -> {
                    snackbarHostState.showSnackbar(errorText)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlyphTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(GlyphShape.button)
                    .background(GlyphTheme.colors.textPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "G",
                    style = GlyphTheme.typography.heading1.copy(
                        color = GlyphTheme.colors.contentOnAccent,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(StringRes.string.auth_title),
                style = GlyphTheme.typography.heading1.copy(color = GlyphTheme.colors.textPrimary),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(StringRes.string.auth_subtitle),
                style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = viewModel::onSignInClick,
                enabled = when (state) {
                    is AuthUiState.Loading -> false
                    is AuthUiState.Ready -> true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = GlyphShape.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlyphTheme.colors.accent,
                    contentColor = GlyphTheme.colors.contentOnAccent,
                ),
            ) {
                when (state) {
                    is AuthUiState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = GlyphTheme.colors.contentOnAccent,
                        strokeWidth = 2.dp,
                    )

                    is AuthUiState.Ready -> Text(
                        text = stringResource(StringRes.string.auth_sign_in_button),
                        style = GlyphTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}
