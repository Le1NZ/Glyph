package ru.glyph.sync.internal.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.glyph.model.NotePermission

@Serializable
internal data class NoteDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("folder_id") val folderId: String? = null,
    @SerialName("tag_ids") val tagIds: List<String> = emptyList(),
    @SerialName("permission") val permission: NotePermission,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)
