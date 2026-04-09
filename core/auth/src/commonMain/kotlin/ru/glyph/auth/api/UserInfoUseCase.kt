package ru.glyph.auth.api

import ru.glyph.auth.api.model.UserInfo
import ru.glyph.utils.ConvertedResult

interface UserInfoUseCase {

    suspend fun invoke(): ConvertedResult<UserInfo>
}