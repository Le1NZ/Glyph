package ru.glyph.auth.internal.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import ru.glyph.auth.internal.network.dto.YandexUserInfoDto
import ru.glyph.network.api.ApiConfig

internal class YandexUserInfoService(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
) {

    suspend fun fetchUserInfo(): Result<YandexUserInfoDto> = runCatching {
        httpClient
            .get(urlString = "${apiConfig.baseUrl}/api/v1/profile")
            .body<YandexUserInfoDto>()
    }
}
