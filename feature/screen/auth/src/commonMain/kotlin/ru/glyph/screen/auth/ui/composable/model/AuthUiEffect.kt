package ru.glyph.screen.auth.ui.composable.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface AuthUiEffect {

    data object ErrorMessage : AuthUiEffect
}