package ru.glyph.sync.internal.network

import ru.glyph.sync.internal.network.dto.FolderDto

internal interface FolderApiService {
    suspend fun getAll(): List<FolderDto>
    suspend fun create(
        id: String,
        name: String,
        color: String,
        parentFolderId: String?,
        createdAt: Long,
        updatedAt: Long,
    ): FolderDto
    suspend fun update(
        id: String,
        name: String,
        color: String,
        parentFolderId: String?,
        updatedAt: Long,
    ): FolderDto
    suspend fun delete(id: String)
}
