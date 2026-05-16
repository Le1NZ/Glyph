package ru.glyph.server.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FolderDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("color") val color: FolderColor,
    @SerialName("parent_folder_id") val parentFolderId: String? = null,
    @SerialName("permission") val permission: FolderPermission,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class CreateFolderRequest(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("color") val color: FolderColor,
    @SerialName("parent_folder_id") val parentFolderId: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class UpdateFolderRequest(
    @SerialName("name") val name: String,
    @SerialName("color") val color: FolderColor,
    @SerialName("parent_folder_id") val parentFolderId: String? = null,
    @SerialName("updated_at") val updatedAt: Long,
)
