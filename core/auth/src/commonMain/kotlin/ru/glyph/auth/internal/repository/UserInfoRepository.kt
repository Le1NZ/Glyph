package ru.glyph.auth.internal.repository

import ru.glyph.auth.api.model.UserInfo

internal interface UserInfoRepository {
    suspend fun getUserInfo(): Result<UserInfo>
}
