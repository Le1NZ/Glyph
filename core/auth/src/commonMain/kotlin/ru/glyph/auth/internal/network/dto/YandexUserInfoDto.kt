package ru.glyph.auth.internal.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class YandexUserInfoDto(
    @SerialName("id") val id: String,
    @SerialName("login") val login: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("default_email") val defaultEmail: String? = null,
    @SerialName("default_avatar_id") val defaultAvatarId: String? = null,
)
