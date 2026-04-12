package ru.glyph.database.api

import kotlinx.coroutines.flow.Flow
import ru.glyph.database.api.entity.NoteEntity

interface NotesRepository {
    fun observeAll(): Flow<List<NoteEntity>>
    suspend fun getById(id: String): NoteEntity?
    suspend fun create(title: String = "", content: String = ""): String
    suspend fun upsert(id: String, title: String, content: String, createdAt: Long, updatedAt: Long)
    suspend fun update(id: String, title: String, content: String)
    suspend fun delete(id: String)
    suspend fun deleteAll()
}
