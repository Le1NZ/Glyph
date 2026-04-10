package ru.glyph.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import ru.glyph.server.database.NotesRepository
import ru.glyph.server.model.CreateNoteRequest
import ru.glyph.server.model.UpdateNoteRequest

fun Route.notesRoutes() {
    authenticate("yandex") {
        route("/api/v1/notes") {

            get {
                val userId = call.principal<UserIdPrincipal>()!!.name
                call.respond(NotesRepository.getAll(userId))
            }

            get("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
                val note = NotesRepository.getById(id, userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(note)
            }

            post {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val request = call.receive<CreateNoteRequest>()
                val note = NotesRepository.create(userId, request)
                call.respond(HttpStatusCode.Created, note)
            }

            put("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
                val request = call.receive<UpdateNoteRequest>()
                val updated = NotesRepository.update(id, userId, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound)
                call.respond(updated)
            }

            delete("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
                val deleted = NotesRepository.delete(id, userId)
                if (deleted) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
