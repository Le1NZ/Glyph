package ru.glyph.auth.api.model

data class UserInfo(
    val id: String,
    val login: String,
    val displayName: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val avatarId: String?,
) {

    val avatarUrl = avatarId?.let { "https://avatars.yandex.net/get-yapic/$it/islands-200" }
}
