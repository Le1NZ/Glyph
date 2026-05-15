package ru.glyph.server.database

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import ru.glyph.server.model.CreateTagRequest
import ru.glyph.server.model.TagDto
import ru.glyph.server.model.UpdateTagRequest

object TagsRepository {

    suspend fun getAll(userId: String): List<TagDto> = query {
        Tags.selectAll()
            .where { Tags.userYandexId eq userId }
            .orderBy(Tags.updatedAt)
            .map { it.toDto() }
    }

    suspend fun getById(id: String, userId: String): TagDto? = query {
        Tags.selectAll()
            .where { (Tags.id eq id) and (Tags.userYandexId eq userId) }
            .firstOrNull()
            ?.toDto()
    }

    suspend fun create(userId: String, request: CreateTagRequest): TagDto = query {
        Tags.insert {
            it[id] = request.id
            it[userYandexId] = userId
            it[name] = request.name
            it[color] = request.color
            it[createdAt] = request.createdAt
            it[updatedAt] = request.updatedAt
        }
        Tags.selectAll()
            .where { Tags.id eq request.id }
            .first()
            .toDto()
    }

    suspend fun update(id: String, userId: String, request: UpdateTagRequest): TagDto? = query {
        val existing = Tags.selectAll()
            .where { (Tags.id eq id) and (Tags.userYandexId eq userId) }
            .firstOrNull()
            ?: return@query null

        if (request.updatedAt < existing[Tags.updatedAt]) {
            return@query existing.toDto()
        }

        Tags.update(
            where = { (Tags.id eq id) and (Tags.userYandexId eq userId) }
        ) {
            it[name] = request.name
            it[color] = request.color
            it[updatedAt] = request.updatedAt
        }
        Tags.selectAll().where { Tags.id eq id }.firstOrNull()?.toDto()
    }

    suspend fun delete(id: String, userId: String): Boolean = query {
        Tags.deleteWhere { (Tags.id eq id) and (Tags.userYandexId eq userId) } > 0
    }

    private suspend fun <T> query(block: () -> T): T =
        newSuspendedTransaction { block() }

    private fun ResultRow.toDto() = TagDto(
        id = this[Tags.id],
        name = this[Tags.name],
        color = this[Tags.color],
        createdAt = this[Tags.createdAt],
        updatedAt = this[Tags.updatedAt],
    )
}
