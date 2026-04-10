package ru.glyph.database.api.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.glyph.database.api.entity.NoteEntity

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Upsert
    suspend fun upsert(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
