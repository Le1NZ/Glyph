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
import ru.glyph.sync.internal.network.dto.TagDto

internal class TagApiServiceImpl(
    private val client: HttpClient,
    config: ApiConfig,
) : TagApiService {

    private val baseUrl = "${config.baseUrl}/api/v1/tags"

    override suspend fun getAll(): List<TagDto> = client.get(baseUrl).body()

    override suspend fun create(
        id: String,
        name: String,
        color: String,
        createdAt: Long,
        updatedAt: Long,
    ): TagDto = client.post(baseUrl) {
        contentType(ContentType.Application.Json)
        setBody(CreateTagRequest(id, name, color, createdAt, updatedAt))
    }.body()

    override suspend fun update(
        id: String,
        name: String,
        color: String,
        updatedAt: Long,
    ): TagDto = client.put("$baseUrl/$id") {
        contentType(ContentType.Application.Json)
        setBody(UpdateTagRequest(name, color, updatedAt))
    }.body()

    override suspend fun delete(id: String) {
        client.delete("$baseUrl/$id")
    }
}

@Serializable
private data class CreateTagRequest(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("color") val color: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
private data class UpdateTagRequest(
    @SerialName("name") val name: String,
    @SerialName("color") val color: String,
    @SerialName("updated_at") val updatedAt: Long,
)
