package ru.glyph.auth.internal.storage

import kotlinx.coroutines.flow.Flow

internal interface AuthTokenStorage {

    val token: Flow<AuthToken?>

    fun getTokenBlocking(): AuthToken?
    suspend fun saveToken(token: AuthToken)
    suspend fun clearToken()
}
