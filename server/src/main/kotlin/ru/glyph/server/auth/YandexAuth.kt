package ru.glyph.server.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.glyph.server.database.NotesRepository
import ru.glyph.server.model.UserProfileDto
import ru.glyph.server.model.YandexUserInfo

private val log = LoggerFactory.getLogger("YandexAuth")

private val yandexHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

fun Application.configureAuth() {
    install(Authentication) {
        bearer("yandex") {
            realm = "Glyph API"
            authHeader { call ->
                call.request.headers["X-Auth-Token"]?.let { token ->
                    HttpAuthHeader.Single("Bearer", token)
                }
            }
            authenticate { credential ->
                val userInfo = validateYandexToken(credential.token) ?: return@authenticate null
                log.info("[SHARE_DEBUG] auth: yandexId=${userInfo.id}, login=${userInfo.login}, defaultEmail=${userInfo.defaultEmail}, emailsList=${userInfo.emails}")
                val emails = buildSet {
                    // defaultEmail and emails list are only present when the OAuth token
                    // has the login:email scope. Add them if available.
                    userInfo.defaultEmail?.lowercase()?.trim()?.let { if (it.isNotBlank()) add(it) }
                    userInfo.emails?.forEach { e -> e.lowercase().trim().let { if (it.isNotBlank()) add(it) } }
                    // Fallback: Yandex always has login@yandex.ru as a valid address,
                    // even when the email scope is not granted.
                    if (isEmpty() && userInfo.login.isNotBlank()) {
                        add("${userInfo.login.lowercase().trim()}@yandex.ru")
                    }
                }.toList()
                log.info("[SHARE_DEBUG] auth: final emails to store=$emails")
                NotesRepository.ensureUser(userInfo.id, emails)
                UserIdPrincipal(userInfo.id)
            }
        }
    }
}

internal suspend fun fetchFullYandexUserInfo(token: String): UserProfileDto? {
    return try {
        val response = yandexHttpClient.get("https://login.yandex.ru/info") {
            header(HttpHeaders.Authorization, "OAuth $token")
            url { parameters.append("format", "json") }
        }
        if (response.status == HttpStatusCode.OK) response.body() else null
    } catch (_: Exception) {
        null
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
