package ru.glyph.auth.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.SignInResult
import ru.glyph.auth.api.model.UserState
import ru.glyph.auth.internal.auth.PlatformAuthProvider
import ru.glyph.auth.internal.auth.PlatformAuthResult
import ru.glyph.auth.internal.storage.AuthToken
import ru.glyph.auth.internal.storage.AuthTokenStorage
import ru.glyph.utils.clock.currentTimeDuration
import ru.glyph.utils.flow.collectIn
import ru.glyph.utils.flow.mapState

internal class UserCenterImpl(
    private val platformAuthProvider: PlatformAuthProvider,
    private val tokenStorage: AuthTokenStorage,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : UserCenter {

    private val tokenState = tokenStorage.token
        .stateIn(
            scope = scope,
            initialValue = tokenStorage.getTokenBlocking(),
            started = SharingStarted.Eagerly,
        ).mapState { token ->
            token?.takeIf { it.isValid() }
        }

    override val authState = MutableStateFlow(
        value = if (tokenState.value == null) {
            UserState.NotAuthorized
        } else {
            UserState.Authorized
        }
    )

    init {
        tokenStorage.token.collectIn(
            scope = scope,
            collector = ::processNewToken,
        )
    }

    override suspend fun signIn(): SignInResult {
        return when (val result = platformAuthProvider.authenticate()) {
            is PlatformAuthResult.Success -> {
                val token = AuthToken(
                    value = result.token,
                    expiresAt = currentTimeDuration().inWholeSeconds + result.expiresIn,
                )
                tokenStorage.saveToken(token)
                authState.value = UserState.Authorized
                SignInResult.Success
            }

            is PlatformAuthResult.Cancelled -> SignInResult.Cancelled
            is PlatformAuthResult.Error -> SignInResult.Error
        }
    }

    override suspend fun signOut() {
        tokenStorage.clearToken()
        authState.value = UserState.NotAuthorized
    }

    override fun getToken(): String? = tokenState.value?.value

    private suspend fun processNewToken(token: AuthToken?) {
        if (token != null && !token.isValid()) {
            tokenStorage.clearToken()
            authState.value = UserState.NotAuthorized
        }
    }
}
