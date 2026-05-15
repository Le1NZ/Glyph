package ru.glyph.server.database

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import ru.glyph.server.model.CreateNoteRequest
import ru.glyph.server.model.NoteDto
import ru.glyph.server.model.UpdateNoteRequest

object NotesRepository {

    suspend fun getAll(userId: String): List<NoteDto> = query {
        val notes = Notes.selectAll()
            .where { Notes.userYandexId eq userId }
            .orderBy(Notes.updatedAt)
            .toList()
            
        val noteIds = notes.map { it[Notes.id] }
        val tagsMap = getTagsForNotes(noteIds)
        
        notes.map { it.toDto(tagsMap[it[Notes.id]] ?: emptyList()) }
    }

    suspend fun getById(id: String, userId: String): NoteDto? = query {
        val row = Notes.selectAll()
            .where { (Notes.id eq id) and (Notes.userYandexId eq userId) }
            .firstOrNull() ?: return@query null
            
        val tags = getTagsForNotes(listOf(id))[id] ?: emptyList()
        row.toDto(tags)
    }

    suspend fun create(userId: String, request: CreateNoteRequest): NoteDto = query {
        Notes.insert {
            it[id] = request.id
            it[userYandexId] = userId
            it[folderId] = request.folderId
            it[title] = request.title
            it[content] = request.content
            it[createdAt] = request.createdAt
            it[updatedAt] = request.updatedAt
        }
        
        updateNoteTags(request.id, request.tagIds)
        
        Notes.selectAll()
            .where { Notes.id eq request.id }
            .first()
            .toDto(request.tagIds)
    }

    suspend fun update(id: String, userId: String, request: UpdateNoteRequest): NoteDto? = query {
        val existing = Notes.selectAll()
            .where { (Notes.id eq id) and (Notes.userYandexId eq userId) }
            .firstOrNull()
            ?: return@query null

        if (request.updatedAt < existing[Notes.updatedAt]) {
            val tags = getTagsForNotes(listOf(id))[id] ?: emptyList()
            return@query existing.toDto(tags)
        }

        Notes.update(
            where = { (Notes.id eq id) and (Notes.userYandexId eq userId) }
        ) {
            it[title] = request.title
            it[content] = request.content
            it[folderId] = request.folderId
            it[updatedAt] = request.updatedAt
        }
        
        updateNoteTags(id, request.tagIds)
        
        Notes.selectAll().where { Notes.id eq id }.firstOrNull()?.toDto(request.tagIds)
    }

    suspend fun delete(id: String, userId: String): Boolean = query {
        Notes.deleteWhere { (Notes.id eq id) and (Notes.userYandexId eq userId) } > 0
    }

    suspend fun ensureUser(yandexId: String) = query {
        val exists = Users.selectAll().where { Users.yandexId eq yandexId }.count() > 0
        if (!exists) {
            Users.insert { it[Users.yandexId] = yandexId }
        }
    }

    private fun getTagsForNotes(noteIds: List<String>): Map<String, List<String>> {
        if (noteIds.isEmpty()) return emptyMap()
        
        val result = mutableMapOf<String, MutableList<String>>()
        NoteTags.selectAll()
            .where { NoteTags.noteId inList noteIds }
            .forEach { row ->
                val noteId = row[NoteTags.noteId]
                val tagId = row[NoteTags.tagId]
                result.getOrPut(noteId) { mutableListOf() }.add(tagId)
            }
        return result
    }
    
    private fun updateNoteTags(noteId: String, tagIds: List<String>) {
        NoteTags.deleteWhere { NoteTags.noteId eq noteId }
        tagIds.forEach { tagId ->
            NoteTags.insert {
                it[NoteTags.noteId] = noteId
                it[NoteTags.tagId] = tagId
            }
        }
    }

    private suspend fun <T> query(block: () -> T): T =
        newSuspendedTransaction { block() }

    private fun ResultRow.toDto(tagIds: List<String>) = NoteDto(
        id = this[Notes.id],
        title = this[Notes.title],
        content = this[Notes.content],
        folderId = this[Notes.folderId],
        tagIds = tagIds,
        createdAt = this[Notes.createdAt],
        updatedAt = this[Notes.updatedAt],
    )
}
