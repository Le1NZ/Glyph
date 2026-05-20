package ru.glyph.share_bottom_sheet.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.glyph.model.NotePermission

class UserNotFoundOnServerException : Exception("User not found on server")

@Serializable
internal data class NoteShareDto(
    @SerialName("email") val email: String,
    @SerialName("permission") val permission: NotePermission,
)

@Serializable
internal data class ShareNoteRequest(
    @SerialName("email") val email: String,
    @SerialName("permission") val permission: NotePermission,
)

internal interface ShareApiService {
    suspend fun getShares(noteId: String): List<NoteShareDto>
    suspend fun addShare(noteId: String, email: String, permission: NotePermission): NoteShareDto
    suspend fun updateShare(noteId: String, email: String, permission: NotePermission): NoteShareDto
    suspend fun removeShare(noteId: String, email: String)
}

internal class ShareApiServiceImpl(
    private val client: HttpClient,
    private val baseUrl: String,
) : ShareApiService {

    override suspend fun getShares(noteId: String): List<NoteShareDto> {
        return client.get("$baseUrl/api/v1/notes/$noteId/shares").body()
    }

    override suspend fun addShare(noteId: String, email: String, permission: NotePermission): NoteShareDto {
        val response: HttpResponse = client.post("$baseUrl/api/v1/notes/$noteId/shares") {
            contentType(ContentType.Application.Json)
            setBody(ShareNoteRequest(email, permission))
        }
        if (response.status == HttpStatusCode.NotFound) throw UserNotFoundOnServerException()
        return response.body()
    }

    override suspend fun updateShare(noteId: String, email: String, permission: NotePermission): NoteShareDto {
        return client.put("$baseUrl/api/v1/notes/$noteId/shares/$email") {
            contentType(ContentType.Application.Json)
            setBody(ShareNoteRequest(email, permission))
        }.body()
    }

    override suspend fun removeShare(noteId: String, email: String) {
        client.delete("$baseUrl/api/v1/notes/$noteId/shares/$email")
    }
}