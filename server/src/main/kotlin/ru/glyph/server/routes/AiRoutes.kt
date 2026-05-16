package ru.glyph.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.glyph.server.ai.AiService
import ru.glyph.server.model.AiGenerateRequest

fun Route.aiRoutes(aiService: AiService) {
    authenticate("yandex") {
        route("/api/ai") {
            post("/generate") {
                val request = call.receive<AiGenerateRequest>()
                val response = aiService.generateText(request)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
