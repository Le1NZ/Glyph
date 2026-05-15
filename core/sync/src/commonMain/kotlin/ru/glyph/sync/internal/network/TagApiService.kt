package ru.glyph.sync.internal.network

import ru.glyph.sync.internal.network.dto.TagDto

internal interface TagApiService {

    suspend fun getAll(): List<TagDto>

    suspend fun create(
        id: String,
        name: String,
        color: String,
        createdAt: Long,
        updatedAt: Long,
    ): TagDto

    suspend fun update(
        id: String,
        name: String,
        color: String,
        updatedAt: Long,
    ): TagDto

    suspend fun delete(id: String)
}
