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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.glyph.network.api.ApiConfig
import ru.glyph.sync.internal.network.dto.NoteDto

internal class NoteApiServiceImpl(
    private val client: HttpClient,
    config: ApiConfig,
) : NoteApiService {

    private val baseUrl = "${config.baseUrl}/api/v1/notes"

    override suspend fun getAll(): List<NoteDto> = client.get(baseUrl).body()

    override suspend fun create(
        id: String,
        title: String,
        content: String,
        createdAt: Long,
        updatedAt: Long,
    ): NoteDto = client.post(baseUrl) {
        contentType(ContentType.Application.Json)
        setBody(CreateNoteRequest(id, title, content, createdAt, updatedAt))
    }.body()

    override suspend fun update(
        id: String,
        title: String,
        content: String,
        updatedAt: Long,
    ): NoteDto = client.put("$baseUrl/$id") {
        contentType(ContentType.Application.Json)
        setBody(UpdateNoteRequest(title, content, updatedAt))
    }.body()

    override suspend fun delete(id: String) {
        client.delete("$baseUrl/$id")
    }
}

@Serializable
private data class CreateNoteRequest(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
private data class UpdateNoteRequest(
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("updated_at") val updatedAt: Long,
)
