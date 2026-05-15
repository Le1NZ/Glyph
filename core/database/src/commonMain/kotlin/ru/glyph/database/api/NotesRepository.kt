package ru.glyph.database.api

import kotlinx.coroutines.flow.Flow
import ru.glyph.model.Note

interface NotesRepository {

    fun observeAll(): Flow<List<Note>>

    fun observeByFolder(folderId: String?): Flow<List<Note>>

    /**
     * Map of folderId -> count of notes inside it. Notes with folderId=null are excluded.
     */
    fun observeFolderCounts(): Flow<Map<String, Int>>

    fun search(query: String): Flow<List<Note>>

    suspend fun getById(id: String): Note?

    /**
     * @return id of created note
     */
    suspend fun create(
        title: String = "",
        content: String = "",
        folderId: String? = null,
    ): String

    suspend fun upsert(
        note: Note
    )

    suspend fun update(
        id: String,
        title: String,
        content: String,
    )

    suspend fun setFolder(id: String, folderId: String?)

    suspend fun setTags(id: String, tagIds: List<String>)

    suspend fun delete(id: String)
    suspend fun deleteAll()
}
