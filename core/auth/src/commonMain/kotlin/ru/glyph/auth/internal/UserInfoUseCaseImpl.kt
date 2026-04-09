package ru.glyph.auth.internal

import ru.glyph.auth.api.UserInfoUseCase
import ru.glyph.auth.api.model.UserInfo
import ru.glyph.auth.internal.repository.UserInfoRepository
import ru.glyph.utils.ConvertedResult
import ru.glyph.utils.toConvertedResult

internal class UserInfoUseCaseImpl(
    repositoryLazy: Lazy<UserInfoRepository>,
) : UserInfoUseCase {

    private val repository by repositoryLazy

    override suspend fun invoke(): ConvertedResult<UserInfo> {
        return repository.getUserInfo().toConvertedResult()
    }
}