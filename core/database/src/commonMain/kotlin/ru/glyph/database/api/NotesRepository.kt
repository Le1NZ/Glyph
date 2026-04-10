package ru.glyph.database.api

import kotlinx.coroutines.flow.Flow
import ru.glyph.database.api.entity.NoteEntity

interface NotesRepository {
    fun observeAll(): Flow<List<NoteEntity>>
    suspend fun getById(id: Long): NoteEntity?
    suspend fun create(title: String = "", content: String = ""): Long
    suspend fun update(id: Long, title: String, content: String)
    suspend fun delete(id: Long)
    suspend fun deleteAll()
}
