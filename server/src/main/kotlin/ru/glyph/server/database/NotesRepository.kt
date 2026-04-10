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
        Notes.selectAll()
            .where { Notes.userYandexId eq userId }
            .orderBy(Notes.updatedAt)
            .map { it.toDto() }
    }

    suspend fun getById(id: Long, userId: String): NoteDto? = query {
        Notes.selectAll()
            .where { (Notes.id eq id) and (Notes.userYandexId eq userId) }
            .firstOrNull()
            ?.toDto()
    }

    suspend fun create(userId: String, request: CreateNoteRequest): NoteDto = query {
        val newId = Notes.insert {
            it[userYandexId] = userId
            it[title] = request.title
            it[content] = request.content
            it[createdAt] = request.createdAt
            it[updatedAt] = request.updatedAt
        }[Notes.id]

        Notes.selectAll()
            .where { Notes.id eq newId }
            .first()
            .toDto()
    }

    suspend fun update(id: Long, userId: String, request: UpdateNoteRequest): NoteDto? = query {
        val updated = Notes.update(
            where = { (Notes.id eq id) and (Notes.userYandexId eq userId) }
        ) {
            it[title] = request.title
            it[content] = request.content
            it[updatedAt] = request.updatedAt
        }
        if (updated == 0) return@query null
        Notes.selectAll().where { Notes.id eq id }.firstOrNull()?.toDto()
    }

    suspend fun delete(id: Long, userId: String): Boolean = query {
        Notes.deleteWhere { (Notes.id eq id) and (Notes.userYandexId eq userId) } > 0
    }

    /** Создаёт запись пользователя при первом входе, если её ещё нет. */
    suspend fun ensureUser(yandexId: String) = query {
        val exists = Users.selectAll().where { Users.yandexId eq yandexId }.count() > 0
        if (!exists) {
            Users.insert { it[Users.yandexId] = yandexId }
        }
    }

    private suspend fun <T> query(block: () -> T): T =
        newSuspendedTransaction { block() }

    private fun ResultRow.toDto() = NoteDto(
        id = this[Notes.id],
        title = this[Notes.title],
        content = this[Notes.content],
        createdAt = this[Notes.createdAt],
        updatedAt = this[Notes.updatedAt],
    )
}
