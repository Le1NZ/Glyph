package ru.glyph.database.internal

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ru.glyph.database.api.NotesRepository
import ru.glyph.database.api.dao.NoteDao
import ru.glyph.database.api.entity.NoteEntity
import ru.glyph.utils.clock.currentTimeDuration

@OptIn(ExperimentalUuidApi::class)
internal class NotesRepositoryImpl(
    private val dao: NoteDao,
) : NotesRepository {

    override fun observeAll(): Flow<List<NoteEntity>> = dao.observeAll()

    override suspend fun getById(id: String): NoteEntity? = dao.getById(id)

    override suspend fun create(title: String, content: String): String {
        val id = Uuid.random().toString()
        val now = currentTimeDuration().inWholeMilliseconds
        dao.upsert(NoteEntity(id = id, title = title, content = content, createdAt = now, updatedAt = now))
        return id
    }

    override suspend fun upsert(id: String, title: String, content: String, createdAt: Long, updatedAt: Long) {
        dao.upsert(NoteEntity(id = id, title = title, content = content, createdAt = createdAt, updatedAt = updatedAt))
    }

    override suspend fun update(id: String, title: String, content: String) {
        val existing = dao.getById(id) ?: return
        dao.upsert(existing.copy(title = title, content = content, updatedAt = currentTimeDuration().inWholeMilliseconds))
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun deleteAll() = dao.deleteAll()
}
