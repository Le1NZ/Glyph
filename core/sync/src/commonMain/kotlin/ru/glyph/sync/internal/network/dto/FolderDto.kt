package ru.glyph.sync.internal.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FolderDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("color") val color: String,
    @SerialName("parent_folder_id") val parentFolderId: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)
