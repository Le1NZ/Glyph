package ru.glyph.database.api

import kotlinx.coroutines.flow.Flow
import ru.glyph.model.Tag

interface TagsRepository {
    fun observeAll(): Flow<List<Tag>>
    suspend fun getById(id: String): Tag?
    suspend fun upsert(tag: Tag)
    suspend fun deleteById(id: String)
    suspend fun deleteAll()
}
