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
import ru.glyph.server.database.FoldersRepository
import ru.glyph.server.model.CreateFolderRequest
import ru.glyph.server.model.UpdateFolderRequest

fun Route.foldersRoutes() {
    authenticate("yandex") {
        route("/api/v1/folders") {

            get {
                val userId = call.principal<UserIdPrincipal>()!!.name
                call.respond(FoldersRepository.getAll(userId))
            }

            get("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
                val folder = FoldersRepository.getById(id, userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(folder)
            }

            post {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val request = call.receive<CreateFolderRequest>()
                val folder = FoldersRepository.create(userId, request)
                call.respond(HttpStatusCode.Created, folder)
            }

            put("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing id")
                val request = call.receive<UpdateFolderRequest>()
                val updated = FoldersRepository.update(id, userId, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound)
                call.respond(updated)
            }

            delete("{id}") {
                val userId = call.principal<UserIdPrincipal>()!!.name
                val id = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
                val deleted = FoldersRepository.delete(id, userId)
                if (deleted) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
