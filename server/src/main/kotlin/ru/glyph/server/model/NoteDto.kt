package ru.glyph.server.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class CreateNoteRequest(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class UpdateNoteRequest(
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
internal data class YandexUserInfo(
    @SerialName("id") val id: String,
    @SerialName("login") val login: String,
)
