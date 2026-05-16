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
import ru.glyph.server.model.NoteShareDto
import ru.glyph.server.model.UpdateNoteRequest

import ru.glyph.server.model.NotePermission
import ru.glyph.server.model.SharedFolderConstants

object NotesRepository {

    suspend fun getAll(userId: String): List<NoteDto> = query {
        val userEmail = Users.selectAll().where { Users.yandexId eq userId }.firstOrNull()?.get(Users.email)

        // Get owned notes
        val ownedNotes = Notes.selectAll()
            .where { Notes.userYandexId eq userId }
            .toList()

        // Get shared notes if user has email
        val sharedNotes = if (userEmail != null) {
            (Notes innerJoin NoteShares).selectAll()
                .where { NoteShares.email eq userEmail }
                .toList()
        } else {
            emptyList()
        }

        val allNotes = ownedNotes + sharedNotes
        val noteIds = allNotes.map { it[Notes.id] }.distinct()
        val tagsMap = getTagsForNotes(noteIds)

        val result = mutableListOf<NoteDto>()
        
        ownedNotes.forEach { row ->
            result.add(row.toDto(tagsMap[row[Notes.id]] ?: emptyList(), permission = NotePermission.WRITE.name))
        }
        
        sharedNotes.forEach { row ->
            val dto = row.toDto(tagsMap[row[Notes.id]] ?: emptyList(), permission = row[NoteShares.permission])
            result.add(dto.copy(folderId = SharedFolderConstants.ID))
        }

        result.sortedBy { it.updatedAt }
    }

    suspend fun getById(id: String, userId: String): NoteDto? = query {
        val userEmail = Users.selectAll().where { Users.yandexId eq userId }.firstOrNull()?.get(Users.email)

        val ownedRow = Notes.selectAll()
            .where { (Notes.id eq id) and (Notes.userYandexId eq userId) }
            .firstOrNull()

        if (ownedRow != null) {
            val tags = getTagsForNotes(listOf(id))[id] ?: emptyList()
            return@query ownedRow.toDto(tags, permission = NotePermission.WRITE.name)
        }

        if (userEmail != null) {
            val sharedRow = (Notes innerJoin NoteShares).selectAll()
                .where { (Notes.id eq id) and (NoteShares.email eq userEmail) }
                .firstOrNull()
            
            if (sharedRow != null) {
                val tags = getTagsForNotes(listOf(id))[id] ?: emptyList()
                return@query sharedRow.toDto(tags, permission = sharedRow[NoteShares.permission]).copy(folderId = SharedFolderConstants.ID)
            }
        }
        
        null
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
            .toDto(request.tagIds, permission = NotePermission.WRITE.name)
    }

    suspend fun update(id: String, userId: String, request: UpdateNoteRequest): NoteDto? = query {
        val userEmail = Users.selectAll().where { Users.yandexId eq userId }.firstOrNull()?.get(Users.email)

        val existingOwned = Notes.selectAll()
            .where { (Notes.id eq id) and (Notes.userYandexId eq userId) }
            .firstOrNull()

        val existingShared = if (existingOwned == null && userEmail != null) {
            (Notes innerJoin NoteShares).selectAll()
                .where { (Notes.id eq id) and (NoteShares.email eq userEmail) }
                .firstOrNull()
        } else {
            null
        }

        val existing = existingOwned ?: existingShared ?: return@query null
        val permission = existingShared?.get(NoteShares.permission) ?: NotePermission.WRITE.name

        if (permission == NotePermission.READ.name) {
            return@query null // Cannot update if only READ access
        }

        if (request.updatedAt < existing[Notes.updatedAt]) {
            val tags = getTagsForNotes(listOf(id))[id] ?: emptyList()
            val dto = existing.toDto(tags, permission)
            return@query if (existingShared != null) dto.copy(folderId = SharedFolderConstants.ID) else dto
        }

        Notes.update(
            where = { Notes.id eq id }
        ) {
            it[title] = request.title
            it[content] = request.content
            // Only owner can change folder
            if (existingOwned != null) {
                it[folderId] = request.folderId
            }
            it[updatedAt] = request.updatedAt
        }
        
        updateNoteTags(id, request.tagIds)
        
        val dto = Notes.selectAll().where { Notes.id eq id }.firstOrNull()?.toDto(request.tagIds, permission)
        return@query if (existingShared != null) dto?.copy(folderId = SharedFolderConstants.ID) else dto
    }

    suspend fun delete(id: String, userId: String): Boolean = query {
        val userEmail = Users.selectAll().where { Users.yandexId eq userId }.firstOrNull()?.get(Users.email)

        // If owner, delete the note
        val deletedNote = Notes.deleteWhere { (Notes.id eq id) and (Notes.userYandexId eq userId) } > 0
        if (deletedNote) return@query true

        // If shared, just remove the share
        if (userEmail != null) {
            return@query NoteShares.deleteWhere { (NoteShares.noteId eq id) and (NoteShares.email eq userEmail) } > 0
        }

        false
    }

    suspend fun getShares(noteId: String, userId: String): List<NoteShareDto>? = query {
        // Check if user owns the note
        val isOwner = Notes.selectAll()
            .where { (Notes.id eq noteId) and (Notes.userYandexId eq userId) }
            .count() > 0
            
        if (!isOwner) return@query null

        NoteShares.selectAll()
            .where { NoteShares.noteId eq noteId }
            .map { NoteShareDto(it[NoteShares.email], NotePermission.valueOf(it[NoteShares.permission])) }
    }

    suspend fun addShare(noteId: String, userId: String, email: String, permission: NotePermission): NoteShareDto? = query {
        val isOwner = Notes.selectAll()
            .where { (Notes.id eq noteId) and (Notes.userYandexId eq userId) }
            .count() > 0
            
        if (!isOwner) return@query null

        // Don't allow sharing with yourself if we know the owner's email
        val ownerEmail = Users.selectAll().where { Users.yandexId eq userId }.firstOrNull()?.get(Users.email)
        if (ownerEmail == email) return@query null

        val existing = NoteShares.selectAll()
            .where { (NoteShares.noteId eq noteId) and (NoteShares.email eq email) }
            .firstOrNull()

        if (existing == null) {
            NoteShares.insert {
                it[NoteShares.noteId] = noteId
                it[NoteShares.email] = email
                it[NoteShares.permission] = permission.name
            }
        } else {
            NoteShares.update({ (NoteShares.noteId eq noteId) and (NoteShares.email eq email) }) {
                it[NoteShares.permission] = permission.name
            }
        }

        NoteShareDto(email, permission)
    }

    suspend fun updateShare(noteId: String, userId: String, email: String, permission: NotePermission): NoteShareDto? = query {
        val isOwner = Notes.selectAll()
            .where { (Notes.id eq noteId) and (Notes.userYandexId eq userId) }
            .count() > 0
            
        if (!isOwner) return@query null

        val updated = NoteShares.update({ (NoteShares.noteId eq noteId) and (NoteShares.email eq email) }) {
            it[NoteShares.permission] = permission.name
        } > 0

        if (updated) NoteShareDto(email, permission) else null
    }

    suspend fun removeShare(noteId: String, userId: String, email: String): Boolean = query {
        val isOwner = Notes.selectAll()
            .where { (Notes.id eq noteId) and (Notes.userYandexId eq userId) }
            .count() > 0
            
        if (!isOwner) return@query false

        NoteShares.deleteWhere { (NoteShares.noteId eq noteId) and (NoteShares.email eq email) } > 0
    }

    suspend fun ensureUser(yandexId: String, email: String? = null) = query {
        val existing = Users.selectAll().where { Users.yandexId eq yandexId }.firstOrNull()
        if (existing == null) {
            Users.insert { 
                it[Users.yandexId] = yandexId 
                it[Users.email] = email
            }
        } else if (email != null && existing[Users.email] != email) {
            Users.update({ Users.yandexId eq yandexId }) {
                it[Users.email] = email
            }
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

    private fun ResultRow.toDto(tagIds: List<String>, permission: String) = NoteDto(
        id = this[Notes.id],
        title = this[Notes.title],
        content = this[Notes.content],
        folderId = this[Notes.folderId],
        tagIds = tagIds,
        permission = NotePermission.valueOf(permission),
        createdAt = this[Notes.createdAt],
        updatedAt = this[Notes.updatedAt],
    )
}
