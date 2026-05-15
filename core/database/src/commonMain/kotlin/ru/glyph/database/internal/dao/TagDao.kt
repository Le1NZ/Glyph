package ru.glyph.database.internal.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.glyph.database.internal.entity.TagEntity

@Dao
internal interface TagDao {

    @Query("SELECT * FROM tags ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: String): TagEntity?

    @Upsert
    suspend fun upsert(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tags")
    suspend fun deleteAll()
}
