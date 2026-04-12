package ru.glyph.screen.auth.ui.composable.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface AuthUiState {

    data object Ready : AuthUiState
    data object Loading : AuthUiState
}
