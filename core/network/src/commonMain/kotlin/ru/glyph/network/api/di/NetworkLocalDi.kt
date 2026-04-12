package ru.glyph.network.api.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import ru.glyph.network.api.ApiConfig
import ru.glyph.network.api.TokenProvider
import ru.glyph.network.internal.oAuthPlugin

object NetworkLocalDi {

    private const val BASE_URL = "https://bba9vmbticscp998bk0n.containers.yandexcloud.net"

    val module = module {
        single { ApiConfig(BASE_URL) }
        single<HttpClient> {
            val tokenProvider = getOrNull<TokenProvider>()
            HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                if (tokenProvider != null) {
                    install(oAuthPlugin(tokenProvider))
                }
            }
        }
    }
}
