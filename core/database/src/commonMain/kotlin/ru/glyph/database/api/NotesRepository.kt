package ru.glyph.database.api

import kotlinx.coroutines.flow.Flow
import ru.glyph.model.Note

interface NotesRepository {

    fun observeAll(): Flow<List<Note>>

    fun search(query: String): Flow<List<Note>>

    suspend fun getById(id: String): Note?

    /**
     * @return id of created note
     */
    suspend fun create(
        title: String = "",
        content: String = "",
    ): String

    suspend fun upsert(
        note: Note
    )

    suspend fun update(
        id: String,
        title: String,
        content: String,
    )

    suspend fun delete(id: String)
    suspend fun deleteAll()
}
