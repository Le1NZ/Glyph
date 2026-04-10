package ru.glyph.database.internal

import kotlinx.coroutines.flow.Flow
import ru.glyph.database.api.NotesRepository
import ru.glyph.database.api.dao.NoteDao
import ru.glyph.database.api.entity.NoteEntity
import ru.glyph.utils.clock.currentTimeDuration

internal class NotesRepositoryImpl(
    private val dao: NoteDao,
) : NotesRepository {

    override fun observeAll(): Flow<List<NoteEntity>> = dao.observeAll()

    override suspend fun getById(id: Long): NoteEntity? = dao.getById(id)

    override suspend fun create(title: String, content: String): Long {
        val now = currentTimeDuration().inWholeMilliseconds
        return dao.upsert(NoteEntity(title = title, content = content, createdAt = now, updatedAt = now))
    }

    override suspend fun update(id: Long, title: String, content: String) {
        val existing = dao.getById(id) ?: return
        dao.upsert(existing.copy(title = title, content = content, updatedAt = currentTimeDuration().inWholeMilliseconds))
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun deleteAll() = dao.deleteAll()
}
