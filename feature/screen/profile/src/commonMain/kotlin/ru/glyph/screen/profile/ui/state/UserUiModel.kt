package ru.glyph.screen.profile.ui.state

internal data class UserUiModel(
    val initials: String,
    val displayName: String,
    val email: String?,
    val avatarUrl: String?,
)
