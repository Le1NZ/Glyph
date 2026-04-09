package ru.glyph.auth.internal.repository

import ru.glyph.auth.api.model.UserInfo
import ru.glyph.auth.internal.network.YandexUserInfoService
import ru.glyph.auth.internal.network.dto.YandexUserInfoDto

internal class UserInfoRepositoryImpl(
    serviceLazy: Lazy<YandexUserInfoService>,
) : UserInfoRepository {

    private val service by serviceLazy

    override suspend fun getUserInfo(): Result<UserInfo> {
        return service.fetchUserInfo().map { it.toUserInfo() }
    }

    private fun YandexUserInfoDto.toUserInfo() = UserInfo(
        id = id,
        login = login,
        displayName = displayName,
        firstName = firstName,
        lastName = lastName,
        email = defaultEmail,
        avatarId = defaultAvatarId,
    )
}
