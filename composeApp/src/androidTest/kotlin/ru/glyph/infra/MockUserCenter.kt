package ru.glyph.infra

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.SignInResult
import ru.glyph.auth.api.model.UserState

internal class MockUserCenter : UserCenter {
    override val authState: StateFlow<UserState> = MutableStateFlow(UserState.Authorized)
    override fun getToken(): String = "fake-token"
    override suspend fun signIn(): SignInResult = SignInResult.Success
    override suspend fun signOut() {}
}