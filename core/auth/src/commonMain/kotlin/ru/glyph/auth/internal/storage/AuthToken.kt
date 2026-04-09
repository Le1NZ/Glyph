package ru.glyph.auth.internal.storage

import kotlinx.serialization.Serializable
import ru.glyph.utils.clock.currentTimeDuration

@Serializable
internal data class AuthToken(
    val value: String,
    val expiresAt: Long,
) {

    fun isValid() = currentTimeDuration().inWholeSeconds < expiresAt
}
