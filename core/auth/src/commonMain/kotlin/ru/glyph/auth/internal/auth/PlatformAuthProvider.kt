package ru.glyph.auth.internal.auth

internal interface PlatformAuthProvider {

    suspend fun authenticate(): PlatformAuthResult
}

internal sealed interface PlatformAuthResult {

    data object Cancelled : PlatformAuthResult

    data class Error(
        val cause: Throwable,
    ) : PlatformAuthResult

    data class Success(
        val token: String,
        val expiresIn: Long,
    ) : PlatformAuthResult
}
