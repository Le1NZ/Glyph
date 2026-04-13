package ru.glyph.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.glyph.auth.api.model.SignInResult
import ru.glyph.auth.api.model.UserState
import ru.glyph.auth.internal.UserCenterImpl
import ru.glyph.auth.internal.auth.PlatformAuthProvider
import ru.glyph.auth.internal.auth.PlatformAuthResult
import ru.glyph.auth.internal.storage.AuthToken
import ru.glyph.auth.internal.storage.AuthTokenStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class UserCenterImplTest {

    // ─── Fakes ───────────────────────────────────────────────────────────────

    private class FakeAuthTokenStorage(
        initial: AuthToken? = null,
    ) : AuthTokenStorage {
        private val _token = MutableStateFlow(initial)

        override val token: Flow<AuthToken?> = _token.asStateFlow()

        var savedToken: AuthToken? = initial
            private set
        var clearCallCount = 0
            private set


        override fun getTokenBlocking(): AuthToken? = _token.value

        override suspend fun saveToken(token: AuthToken) {
            savedToken = token
            _token.value = token
        }

        override suspend fun clearToken() {
            clearCallCount++
            savedToken = null
            _token.value = null
        }
    }

    private class FakePlatformAuthProvider(
        private val result: PlatformAuthResult,
    ) : PlatformAuthProvider {
        override suspend fun authenticate() = result
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun validToken() = AuthToken(value = "token-abc", expiresAt = Long.MAX_VALUE)
    private fun expiredToken() = AuthToken(value = "token-old", expiresAt = 1L)

    private fun createUserCenter(
        authProvider: PlatformAuthProvider = FakePlatformAuthProvider(PlatformAuthResult.Cancelled),
        storage: AuthTokenStorage = FakeAuthTokenStorage(),
        scope: CoroutineScope,
    ) = UserCenterImpl(authProvider, storage, scope)

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial state is NotAuthorized when no token in storage`() = runTest {
        val center = createUserCenter(storage = FakeAuthTokenStorage(initial = null), scope = backgroundScope)

        assertEquals(UserState.NotAuthorized, center.authState.value)
    }

    @Test
    fun `initial state is Authorized when valid token exists`() = runTest {
        val center = createUserCenter(
            storage = FakeAuthTokenStorage(initial = validToken()),
            scope = backgroundScope,
        )

        assertEquals(UserState.Authorized, center.authState.value)
    }

    @Test
    fun `initial state is NotAuthorized when only expired token in storage`() = runTest {
        val center = createUserCenter(
            storage = FakeAuthTokenStorage(initial = expiredToken()),
            scope = backgroundScope,
        )

        assertEquals(UserState.NotAuthorized, center.authState.value)
    }

    // ─── signIn ───────────────────────────────────────────────────────────────

    @Test
    fun `signIn success sets Authorized state and saves token`() = runTest {
        val storage = FakeAuthTokenStorage()
        val center = createUserCenter(
            authProvider = FakePlatformAuthProvider(
                PlatformAuthResult.Success(token = "access-token", expiresIn = 3600L)
            ),
            storage = storage,
            scope = backgroundScope,
        )

        val result = center.signIn()

        assertEquals(SignInResult.Success, result)
        assertEquals(UserState.Authorized, center.authState.value)
        assertEquals("access-token", storage.savedToken?.value)
    }

    @Test
    fun `signIn cancelled returns Cancelled and keeps NotAuthorized state`() = runTest {
        val center = createUserCenter(
            authProvider = FakePlatformAuthProvider(PlatformAuthResult.Cancelled),
            scope = backgroundScope,
        )

        val result = center.signIn()

        assertEquals(SignInResult.Cancelled, result)
        assertEquals(UserState.NotAuthorized, center.authState.value)
    }

    @Test
    fun `signIn error returns Error and keeps NotAuthorized state`() = runTest {
        val center = createUserCenter(
            authProvider = FakePlatformAuthProvider(
                PlatformAuthResult.Error(RuntimeException("auth error"))
            ),
            scope = backgroundScope,
        )

        val result = center.signIn()

        assertEquals(SignInResult.Error, result)
        assertEquals(UserState.NotAuthorized, center.authState.value)
    }

    // ─── signOut ──────────────────────────────────────────────────────────────

    @Test
    fun `signOut clears token and sets NotAuthorized`() = runTest {
        val storage = FakeAuthTokenStorage(initial = validToken())
        val center = createUserCenter(storage = storage, scope = backgroundScope)

        assertEquals(UserState.Authorized, center.authState.value)
        center.signOut()

        assertEquals(UserState.NotAuthorized, center.authState.value)
        assertEquals(1, storage.clearCallCount)
        assertNull(storage.savedToken)
    }

    // ─── getToken ─────────────────────────────────────────────────────────────

    @Test
    fun `getToken returns null when not authorized`() = runTest {
        val center = createUserCenter(scope = backgroundScope)

        assertNull(center.getToken())
    }

    @Test
    fun `getToken returns token value when authorized`() = runTest {
        val storage = FakeAuthTokenStorage(initial = validToken())
        val center = createUserCenter(storage = storage, scope = backgroundScope)

        assertEquals("token-abc", center.getToken())
    }

    // ─── Expired token detection ──────────────────────────────────────────────
    // These tests use UnconfinedTestDispatcher so that collectIn coroutines
    // inside UserCenterImpl start immediately without needing advanceUntilIdle().

    @Test
    fun `expired token in storage at startup triggers clearToken`() = runTest(UnconfinedTestDispatcher()) {
        // Real-world scenario: app starts with a previously-cached token that has since expired.
        // UserCenterImpl.processNewToken detects it via collectIn and calls clearToken.
        val storage = FakeAuthTokenStorage(initial = expiredToken())
        val center = createUserCenter(storage = storage, scope = backgroundScope)

        assertEquals(UserState.NotAuthorized, center.authState.value)
        assertEquals(1, storage.clearCallCount)
        assertNull(storage.savedToken)
    }

    @Test
    fun `null token from storage does not trigger clearToken`() = runTest(UnconfinedTestDispatcher()) {
        val storage = FakeAuthTokenStorage(initial = null)
        createUserCenter(storage = storage, scope = backgroundScope)

        assertEquals(0, storage.clearCallCount)
    }
}
