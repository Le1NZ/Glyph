package ru.glyph.auth.api.model

sealed interface SignInResult {

    data object Cancelled : SignInResult
    data object Error : SignInResult
    data object Success : SignInResult
}