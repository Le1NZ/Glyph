package ru.glyph.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import ru.glyph.server.auth.configureAuth
import ru.glyph.server.database.configureDatabase
import ru.glyph.server.routes.notesRoutes

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to cause.message))
        }
    }

    configureDatabase()
    configureAuth()

    routing {
        notesRoutes()
    }
}
