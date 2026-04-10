package ru.glyph.sync.internal.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NoteDto(
    @SerialName("id") val id: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)
