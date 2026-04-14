package ru.glyph.sync.internal.network

import ru.glyph.sync.internal.network.dto.NoteDto

internal interface NoteApiService {
    suspend fun getAll(): List<NoteDto>
    suspend fun create(id: String, title: String, content: String, createdAt: Long, updatedAt: Long): NoteDto
    suspend fun update(id: String, title: String, content: String, updatedAt: Long): NoteDto
    suspend fun delete(id: String)
}
