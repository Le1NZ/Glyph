package ru.glyph.database.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.glyph.database.api.NotesRepository
import ru.glyph.database.internal.converter.toDomain
import ru.glyph.database.internal.converter.toEntity
import ru.glyph.database.internal.dao.NoteDao
import ru.glyph.database.internal.entity.NoteEntity
import ru.glyph.model.Note
import ru.glyph.utils.clock.currentTimeDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class NotesRepositoryImpl(
    private val dao: NoteDao,
) : NotesRepository {

    override fun observeAll(): Flow<List<Note>> {
        return dao.observeAll()
            .map { notes -> notes.map(NoteEntity::toDomain) }
    }

    override fun search(query: String): Flow<List<Note>> {
        val currentQuery = query.trim().lowercase()
        return dao.observeAll().map { notes ->
            notes.map(NoteEntity::toDomain)
                .filter { note ->
                    val title = note.title.lowercase()
                    val content = note.content.lowercase()
                    title.contains(currentQuery) || content.contains(currentQuery)
                }
        }
    }

    override suspend fun getById(id: String): Note? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun create(
        title: String,
        content: String,
    ): String {
        val id = Uuid.random().toString()
        val now = currentTimeDuration().inWholeMilliseconds

        val note = NoteEntity(
            id = id,
            title = title,
            content = content,
            createdAt = now,
            updatedAt = now,
        )

        dao.upsert(note)
        return id
    }

    override suspend fun upsert(
        note: Note,
    ) {
        dao.upsert(note.toEntity())
    }

    override suspend fun update(
        id: String,
        title: String,
        content: String,
    ) {
        val existing = dao.getById(id) ?: return
        val updated = existing.copy(
            title = title,
            content = content,
            updatedAt = currentTimeDuration().inWholeMilliseconds,
        )

        dao.upsert(updated)
    }

    override suspend fun delete(id: String) = dao.deleteById(id)
    override suspend fun deleteAll() = dao.deleteAll()
}
