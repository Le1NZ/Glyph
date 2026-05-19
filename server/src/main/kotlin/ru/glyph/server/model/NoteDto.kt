package ru.glyph.server.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("folder_id") val folderId: String? = null,
    @SerialName("tag_ids") val tagIds: List<String> = emptyList(),
    @SerialName("permission") val permission: NotePermission,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class NoteShareDto(
    @SerialName("email") val email: String,
    @SerialName("permission") val permission: NotePermission,
)

@Serializable
data class ShareNoteRequest(
    @SerialName("email") val email: String,
    @SerialName("permission") val permission: NotePermission,
)

@Serializable
data class CreateNoteRequest(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("folder_id") val folderId: String? = null,
    @SerialName("tag_ids") val tagIds: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class UpdateNoteRequest(
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("folder_id") val folderId: String? = null,
    @SerialName("tag_ids") val tagIds: List<String> = emptyList(),
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
internal data class YandexUserInfo(
    @SerialName("id") val id: String,
    @SerialName("login") val login: String,
    @SerialName("default_email") val defaultEmail: String? = null,
    @SerialName("emails") val emails: List<String>? = null,
)

@Serializable
data class UserProfileDto(
    @SerialName("id") val id: String,
    @SerialName("login") val login: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("default_email") val defaultEmail: String? = null,
    @SerialName("default_avatar_id") val defaultAvatarId: String? = null,
)
