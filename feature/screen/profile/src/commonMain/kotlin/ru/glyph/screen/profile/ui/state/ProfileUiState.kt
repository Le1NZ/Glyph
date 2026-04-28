package ru.glyph.screen.profile.ui.state

internal sealed interface ProfileUiState {

    data object Loading : ProfileUiState
    data object Error : ProfileUiState

    data class Success(
        val user: UserUiModel,
    ) : ProfileUiState
}
