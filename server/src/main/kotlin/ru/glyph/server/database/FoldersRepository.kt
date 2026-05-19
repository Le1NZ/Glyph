package ru.glyph.server.database

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import ru.glyph.server.model.CreateFolderRequest
import ru.glyph.server.model.FolderDto
import ru.glyph.server.model.UpdateFolderRequest
import ru.glyph.server.model.FolderColor
import ru.glyph.server.model.SharedFolderConstants
import ru.glyph.server.model.FolderPermission

object FoldersRepository {

    suspend fun getAll(userId: String): List<FolderDto> = query {
        val folders = Folders.selectAll()
            .where { Folders.userYandexId eq userId }
            .orderBy(Folders.createdAt)
            .map { it.toDto(permission = FolderPermission.WRITE) }
            .toMutableList()

        val sharedCount = (Notes innerJoin NoteShares).selectAll()
            .where { NoteShares.yandexId eq userId }
            .count()

        if (sharedCount > 0) {
            folders.add(
                FolderDto(
                    id = SharedFolderConstants.ID,
                    name = SharedFolderConstants.NAME,
                    color = SharedFolderConstants.COLOR,
                    parentFolderId = null,
                    permission = FolderPermission.READ,
                    createdAt = 0,
                    updatedAt = 0,
                )
            )
        }

        folders
    }

    suspend fun getById(id: String, userId: String): FolderDto? = query {
        Folders.selectAll()
            .where { (Folders.id eq id) and (Folders.userYandexId eq userId) }
            .firstOrNull()
            ?.toDto(permission = FolderPermission.WRITE)
    }

    suspend fun create(userId: String, request: CreateFolderRequest): FolderDto = query {
        Folders.insert {
            it[id] = request.id
            it[userYandexId] = userId
            it[name] = request.name
            it[color] = request.color.name
            it[parentFolderId] = request.parentFolderId
            it[createdAt] = request.createdAt
            it[updatedAt] = request.updatedAt
        }
        Folders.selectAll()
            .where { Folders.id eq request.id }
            .first()
            .toDto(permission = FolderPermission.WRITE)
    }

    suspend fun update(id: String, userId: String, request: UpdateFolderRequest): FolderDto? = query {
        val existing = Folders.selectAll()
            .where { (Folders.id eq id) and (Folders.userYandexId eq userId) }
            .firstOrNull()
            ?: return@query null

        if (request.updatedAt < existing[Folders.updatedAt]) {
            return@query existing.toDto(permission = FolderPermission.WRITE)
        }

        Folders.update(
            where = { (Folders.id eq id) and (Folders.userYandexId eq userId) }
        ) {
            it[name] = request.name
            it[color] = request.color.name
            it[parentFolderId] = request.parentFolderId
            it[updatedAt] = request.updatedAt
        }
        Folders.selectAll().where { Folders.id eq id }.firstOrNull()?.toDto(permission = FolderPermission.WRITE)
    }

    suspend fun delete(id: String, userId: String): Boolean = query {
        Folders.deleteWhere { (Folders.id eq id) and (Folders.userYandexId eq userId) } > 0
    }

    private suspend fun <T> query(block: () -> T): T =
        newSuspendedTransaction { block() }

    private fun ResultRow.toDto(permission: FolderPermission) = FolderDto(
        id = this[Folders.id],
        name = this[Folders.name],
        color = FolderColor.valueOf(this[Folders.color]),
        parentFolderId = this[Folders.parentFolderId],
        permission = permission,
        createdAt = this[Folders.createdAt],
        updatedAt = this[Folders.updatedAt],
    )
}
