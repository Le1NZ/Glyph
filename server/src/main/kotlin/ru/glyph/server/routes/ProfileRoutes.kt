package ru.glyph.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import ru.glyph.server.auth.fetchFullYandexUserInfo

fun Route.profileRoutes() {
    authenticate("yandex") {
        get("/api/v1/profile") {
            val token = call.request.headers["X-Auth-Token"]!!
            val profile = fetchFullYandexUserInfo(token)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(profile)
        }
    }
}
