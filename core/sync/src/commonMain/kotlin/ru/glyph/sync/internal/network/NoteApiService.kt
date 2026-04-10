package ru.glyph.sync.internal.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ru.glyph.sync.internal.network.dto.NoteDto

/**
 * REST API:
 *   GET    /api/v1/notes          → List<NoteDto>
 *   POST   /api/v1/notes          → NoteDto
 *   PUT    /api/v1/notes/{id}     → NoteDto
 *   DELETE /api/v1/notes/{id}     → 204
 */
internal class NoteApiService(
    private val client: HttpClient,
) {
    private val baseUrl = "https://api.glyph.ru/api/v1/notes"  // TODO: inject base URL

    suspend fun getAll(): List<NoteDto> = client.get(baseUrl).body()

    suspend fun create(note: NoteDto): NoteDto =
        client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(note)
        }.body()

    suspend fun update(id: String, note: NoteDto): NoteDto =
        client.put("$baseUrl/$id") {
            contentType(ContentType.Application.Json)
            setBody(note)
        }.body()

    suspend fun delete(id: String) = client.delete("$baseUrl/$id")
}
