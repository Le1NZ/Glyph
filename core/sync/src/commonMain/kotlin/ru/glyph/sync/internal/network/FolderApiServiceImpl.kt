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
import ru.glyph.sync.internal.network.dto.FolderDto

internal class FolderApiServiceImpl(
    private val client: HttpClient,
    config: ApiConfig,
) : FolderApiService {

    private val baseUrl = "${config.baseUrl}/api/v1/folders"

    override suspend fun getAll(): List<FolderDto> = client.get(baseUrl).body()

    override suspend fun create(
        id: String,
        name: String,
        color: String,
        parentFolderId: String?,
        createdAt: Long,
        updatedAt: Long,
    ): FolderDto = client.post(baseUrl) {
        contentType(ContentType.Application.Json)
        setBody(CreateFolderRequest(id, name, color, parentFolderId, createdAt, updatedAt))
    }.body()

    override suspend fun update(
        id: String,
        name: String,
        color: String,
        parentFolderId: String?,
        updatedAt: Long,
    ): FolderDto = client.put("$baseUrl/$id") {
        contentType(ContentType.Application.Json)
        setBody(UpdateFolderRequest(name, color, parentFolderId, updatedAt))
    }.body()

    override suspend fun delete(id: String) {
        client.delete("$baseUrl/$id")
    }
}

@Serializable
private data class CreateFolderRequest(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("color") val color: String,
    @SerialName("parent_folder_id") val parentFolderId: String?,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
private data class UpdateFolderRequest(
    @SerialName("name") val name: String,
    @SerialName("color") val color: String,
    @SerialName("parent_folder_id") val parentFolderId: String?,
    @SerialName("updated_at") val updatedAt: Long,
)
