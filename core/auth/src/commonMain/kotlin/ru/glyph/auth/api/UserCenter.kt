package ru.glyph.auth.api

import kotlinx.coroutines.flow.StateFlow
import ru.glyph.auth.api.model.SignInResult
import ru.glyph.auth.api.model.UserState

interface UserCenter {

    val authState: StateFlow<UserState>

    fun getToken(): String?

    suspend fun signIn(): SignInResult
    suspend fun signOut()
}
