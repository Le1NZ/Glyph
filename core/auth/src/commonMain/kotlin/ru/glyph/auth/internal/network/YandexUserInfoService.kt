package ru.glyph.auth.internal.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import ru.glyph.auth.internal.network.dto.YandexUserInfoDto

internal class YandexUserInfoService(
    private val httpClient: HttpClient,
) {

    suspend fun fetchUserInfo(): Result<YandexUserInfoDto> = runCatching {
        httpClient
            .get(urlString = "https://login.yandex.ru/info")
            .body<YandexUserInfoDto>()
    }
}
