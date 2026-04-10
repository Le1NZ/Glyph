package ru.glyph.server.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import kotlinx.serialization.json.Json
import ru.glyph.server.database.NotesRepository
import ru.glyph.server.model.YandexUserInfo

private val yandexHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

fun Application.configureAuth() {
    install(Authentication) {
        bearer("yandex") {
            realm = "Glyph API"
            authenticate { credential ->
                val userInfo = validateYandexToken(credential.token) ?: return@authenticate null
                NotesRepository.ensureUser(userInfo.id)
                UserIdPrincipal(userInfo.id)
            }
        }
    }
}

private suspend fun validateYandexToken(token: String): YandexUserInfo? {
    return try {
        val response = yandexHttpClient.get("https://login.yandex.ru/info") {
            header(HttpHeaders.Authorization, "OAuth $token")
            url { parameters.append("format", "json") }
        }
        if (response.status == HttpStatusCode.OK) response.body() else null
    } catch (e: Exception) {
        null
    }
}
