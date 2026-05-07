package ru.glyph.database.internal.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.glyph.database.internal.entity.NoteEntity

@Dao
internal interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folderId IS NULL ORDER BY updatedAt DESC")
    fun observeWithoutFolder(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY updatedAt DESC")
    fun observeByFolder(folderId: String): Flow<List<NoteEntity>>

    @Query("SELECT folderId, COUNT(*) AS count FROM notes WHERE folderId IS NOT NULL GROUP BY folderId")
    fun observeFolderCounts(): Flow<List<FolderNoteCount>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}

internal data class FolderNoteCount(
    val folderId: String,
    val count: Int,
)
