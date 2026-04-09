package ru.glyph.auth.api.model

sealed interface UserState {
    data object NotAuthorized : UserState
    data object Authorized : UserState
}
