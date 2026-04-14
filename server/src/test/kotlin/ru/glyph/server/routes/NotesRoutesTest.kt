package ru.glyph.server.routes

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import ru.glyph.server.database.NotesRepository
import ru.glyph.server.database.Notes
import ru.glyph.server.database.Users
import ru.glyph.server.model.CreateNoteRequest
import ru.glyph.server.model.NoteDto
import ru.glyph.server.model.UpdateNoteRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NotesRoutesTest {

    private val testUser = "test-user-id"

    @BeforeTest
    fun setUp() {
        Database.connect(
            url = "jdbc:h2:mem:routestest;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction { SchemaUtils.create(Users, Notes) }
    }

    @AfterTest
    fun tearDown() {
        transaction { SchemaUtils.drop(Notes, Users) }
    }

    // ─── Test application setup ───────────────────────────────────────────────

    /**
     * Installs a test auth provider that maps the "X-Auth-Token" bearer value directly
     * to a [UserIdPrincipal], bypassing the real Yandex OAuth call.
     */
    private fun Application.testModule() {
        install(ServerContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        }
        install(Authentication) {
            bearer("yandex") {
                authHeader { call ->
                    call.request.headers["X-Auth-Token"]?.let { token ->
                        io.ktor.http.auth.HttpAuthHeader.Single("Bearer", token)
                    }
                }
                authenticate { credential ->
                    NotesRepository.ensureUser(credential.token)
                    UserIdPrincipal(credential.token)
                }
            }
        }
        routing { notesRoutes() }
    }

    private fun buildClient() = testApplication {
        application { testModule() }
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        client
    }

    /** Helper to add X-Auth-Token header to every request. */
    private fun io.ktor.client.request.HttpRequestBuilder.auth(userId: String = testUser) {
        header("X-Auth-Token", userId)
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    @Test
    fun `GET notes returns 200 and empty list for new user`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val response = client.get("/api/v1/notes") { auth() }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<NoteDto>>()
        assertEquals(emptyList(), body)
    }

    @Test
    fun `POST note returns 201 with created note`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val request = CreateNoteRequest(
            id = "note-1",
            title = "Заголовок",
            content = "Содержимое",
            createdAt = 1000L,
            updatedAt = 2000L,
        )
        val response = client.post("/api/v1/notes") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<NoteDto>()
        assertEquals("note-1", body.id)
        assertEquals("Заголовок", body.title)
        assertEquals("Содержимое", body.content)
    }

    @Test
    fun `GET notes returns 200 with list after creating notes`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        client.post("/api/v1/notes") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest("n1", "T1", "C1", 1L, 1L))
        }
        client.post("/api/v1/notes") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest("n2", "T2", "C2", 2L, 2L))
        }

        val response = client.get("/api/v1/notes") { auth() }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<NoteDto>>()
        assertEquals(2, body.size)
    }

    @Test
    fun `GET note by id returns 200 with note`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        client.post("/api/v1/notes") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest("n1", "Title", "Content", 1L, 1L))
        }

        val response = client.get("/api/v1/notes/n1") { auth() }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NoteDto>()
        assertEquals("n1", body.id)
    }

    @Test
    fun `GET note by id returns 404 for missing note`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val response = client.get("/api/v1/notes/non-existent") { auth() }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET note returns 404 when note belongs to another user`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        // Create as testUser
        client.post("/api/v1/notes") {
            auth(testUser)
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest("n1", "T", "C", 1L, 1L))
        }

        // Try to read as different user
        val response = client.get("/api/v1/notes/n1") { auth("other-user") }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT note updates and returns 200 with updated note`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        client.post("/api/v1/notes") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest("n1", "Old Title", "Old Content", 1L, 1L))
        }

        val response = client.put("/api/v1/notes/n1") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(UpdateNoteRequest("New Title", "New Content", 9999L))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NoteDto>()
        assertEquals("New Title", body.title)
        assertEquals("New Content", body.content)
        assertEquals(9999L, body.updatedAt)
    }

    @Test
    fun `PUT note returns 404 for note belonging to another user`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        client.post("/api/v1/notes") {
            auth(testUser)
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest("n1", "T", "C", 1L, 1L))
        }

        val response = client.put("/api/v1/notes/n1") {
            auth("other-user")
            contentType(ContentType.Application.Json)
            setBody(UpdateNoteRequest("X", "X", 1L))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE note returns 204 NoContent`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        client.post("/api/v1/notes") {
            auth()
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest("n1", "T", "C", 1L, 1L))
        }

        val response = client.delete("/api/v1/notes/n1") { auth() }

        assertEquals(HttpStatusCode.NoContent, response.status)
        // Verify deletion: subsequent GET should 404
        val getResponse = client.get("/api/v1/notes/n1") { auth() }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `DELETE note returns 404 for non-existent id`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val response = client.delete("/api/v1/notes/non-existent") { auth() }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `unauthenticated request returns 401`() = testApplication {
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json() } }

        val response = client.get("/api/v1/notes") // no X-Auth-Token header

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
