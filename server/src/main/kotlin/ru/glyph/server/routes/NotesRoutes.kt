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

            // GET /api/v1/notes — список всех заметок пользователя
            get {
                val userId = call.principal<UserIdPrincipal>()!!.name
                call.respond(NotesRepository.getAll(userId))
            }

            // GET /api/v1/notes/{id}
            get("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")
                val note = NotesRepository.getById(id, userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(note)
            }

            // POST /api/v1/notes — создать заметку
            post {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val request = call.receive<CreateNoteRequest>()
                val note = NotesRepository.create(userId, request)
                call.respond(HttpStatusCode.Created, note)
            }

            // PUT /api/v1/notes/{id} — обновить заметку
            put("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid id")
                val request = call.receive<UpdateNoteRequest>()
                val updated = NotesRepository.update(id, userId, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound)
                call.respond(updated)
            }

            // DELETE /api/v1/notes/{id}
            delete("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")
                val deleted = NotesRepository.delete(id, userId)
                if (deleted) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
