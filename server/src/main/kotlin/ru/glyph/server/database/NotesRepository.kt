package ru.glyph.server.database

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import ru.glyph.server.model.CreateNoteRequest
import ru.glyph.server.model.NoteDto
import ru.glyph.server.model.NoteShareDto
import ru.glyph.server.model.UpdateNoteRequest
import ru.glyph.server.model.NotePermission
import ru.glyph.server.model.SharedFolderConstants

class TargetUserNotFoundException(email: String) : Exception("User '$email' not found")

object NotesRepository {

    private val log = LoggerFactory.getLogger(NotesRepository::class.java)

    suspend fun getAll(userId: String): List<NoteDto> = query {
        val ownedNotes = Notes.selectAll()
            .where { Notes.userYandexId eq userId }
            .toList()

        val sharedNotes = (Notes innerJoin NoteShares).selectAll()
            .where { NoteShares.yandexId eq userId }
            .toList()

        val allNoteIds = (ownedNotes + sharedNotes).map { it[Notes.id] }.distinct()
        val tagsMap = getTagsForNotes(allNoteIds)

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
        val ownedRow = Notes.selectAll()
            .where { (Notes.id eq id) and (Notes.userYandexId eq userId) }
            .firstOrNull()

        if (ownedRow != null) {
            val tags = getTagsForNotes(listOf(id))[id] ?: emptyList()
            return@query ownedRow.toDto(tags, permission = NotePermission.WRITE.name)
        }

        val sharedRow = (Notes innerJoin NoteShares).selectAll()
            .where { (Notes.id eq id) and (NoteShares.yandexId eq userId) }
            .firstOrNull()

        if (sharedRow != null) {
            val tags = getTagsForNotes(listOf(id))[id] ?: emptyList()
            return@query sharedRow.toDto(tags, permission = sharedRow[NoteShares.permission]).copy(folderId = SharedFolderConstants.ID)
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
        val existingOwned = Notes.selectAll()
            .where { (Notes.id eq id) and (Notes.userYandexId eq userId) }
            .firstOrNull()

        val existingShared = if (existingOwned == null) {
            (Notes innerJoin NoteShares).selectAll()
                .where { (Notes.id eq id) and (NoteShares.yandexId eq userId) }
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
        
        // Only owner can change tags
        if (existingOwned != null) {
            updateNoteTags(id, request.tagIds)
        }
        
        val tags = getTagsForNotes(listOf(id))[id] ?: emptyList()
        val dto = Notes.selectAll().where { Notes.id eq id }.firstOrNull()?.toDto(tags, permission)
        return@query if (existingShared != null) dto?.copy(folderId = SharedFolderConstants.ID) else dto
    }

    suspend fun delete(id: String, userId: String): Boolean = query {
        val deletedNote = Notes.deleteWhere { (Notes.id eq id) and (Notes.userYandexId eq userId) } > 0
        if (deletedNote) return@query true
        // Shared user deleting = remove only their share entry
        NoteShares.deleteWhere { (NoteShares.noteId eq id) and (NoteShares.yandexId eq userId) } > 0
    }

    suspend fun getShares(noteId: String, userId: String): List<NoteShareDto>? = query {
        val isOwner = Notes.selectAll()
            .where { (Notes.id eq noteId) and (Notes.userYandexId eq userId) }
            .count() > 0
        if (!isOwner) return@query null

        NoteShares.selectAll()
            .where { NoteShares.noteId eq noteId }
            .map { NoteShareDto(it[NoteShares.displayEmail], NotePermission.valueOf(it[NoteShares.permission])) }
    }

    /**
     * Resolves the target email to a yandexId, then stores the share by yandexId.
     * Returns null if the caller is not the owner, if it's a self-share, or if the
     * target user has never opened the app (no record in Users table).
     * Throws [TargetUserNotFoundException] when the target email is not found.
     */
    suspend fun addShare(noteId: String, userId: String, email: String, permission: NotePermission): NoteShareDto? = query {
        val lowerEmail = email.trim().lowercase()
        log.info("[SHARE_DEBUG] addShare: noteId=$noteId, ownerYandexId=$userId, targetEmail=$lowerEmail")

        val isOwner = Notes.selectAll()
            .where { (Notes.id eq noteId) and (Notes.userYandexId eq userId) }
            .count() > 0
        if (!isOwner) {
            log.warn("[SHARE_DEBUG] addShare: REJECTED - not the owner")
            return@query null
        }

        // Resolve target email → yandexId
        val targetYandexId = findYandexIdByEmail(lowerEmail)
        if (targetYandexId == null) {
            log.warn("[SHARE_DEBUG] addShare: target email '$lowerEmail' not found in Users table")
            throw TargetUserNotFoundException(email)
        }

        // Prevent self-share
        if (targetYandexId == userId) {
            log.warn("[SHARE_DEBUG] addShare: REJECTED - self-share")
            return@query null
        }

        log.info("[SHARE_DEBUG] addShare: resolved targetEmail=$lowerEmail → yandexId=$targetYandexId")

        val existing = NoteShares.selectAll()
            .where { (NoteShares.noteId eq noteId) and (NoteShares.yandexId eq targetYandexId) }
            .firstOrNull()

        if (existing == null) {
            NoteShares.insert {
                it[NoteShares.noteId] = noteId
                it[NoteShares.yandexId] = targetYandexId
                it[NoteShares.displayEmail] = lowerEmail
                it[NoteShares.permission] = permission.name
            }
        } else {
            NoteShares.update({ (NoteShares.noteId eq noteId) and (NoteShares.yandexId eq targetYandexId) }) {
                it[NoteShares.permission] = permission.name
            }
        }

        NoteShareDto(lowerEmail, permission)
    }

    suspend fun updateShare(noteId: String, userId: String, email: String, permission: NotePermission): NoteShareDto? = query {
        val lowerEmail = email.trim().lowercase()
        val isOwner = Notes.selectAll()
            .where { (Notes.id eq noteId) and (Notes.userYandexId eq userId) }
            .count() > 0
        if (!isOwner) return@query null

        val targetYandexId = findYandexIdByEmail(lowerEmail) ?: return@query null

        val updated = NoteShares.update({ (NoteShares.noteId eq noteId) and (NoteShares.yandexId eq targetYandexId) }) {
            it[NoteShares.permission] = permission.name
        } > 0

        if (updated) NoteShareDto(lowerEmail, permission) else null
    }

    suspend fun removeShare(noteId: String, userId: String, email: String): Boolean = query {
        val lowerEmail = email.trim().lowercase()
        val isOwner = Notes.selectAll()
            .where { (Notes.id eq noteId) and (Notes.userYandexId eq userId) }
            .count() > 0
        if (!isOwner) return@query false

        val targetYandexId = findYandexIdByEmail(lowerEmail) ?: return@query false
        NoteShares.deleteWhere { (NoteShares.noteId eq noteId) and (NoteShares.yandexId eq targetYandexId) } > 0
    }

    /**
     * Looks up a user's yandexId by email.
     * The email column stores comma-separated lowercase emails, so we check via LIKE.
     * Emails are not substrings of each other in practice, making this safe.
     */
    private fun findYandexIdByEmail(lowerEmail: String): String? =
        Users.selectAll()
            .where { Users.email.lowerCase() like "%$lowerEmail%" }
            .firstOrNull()
            ?.get(Users.yandexId)

    suspend fun ensureUser(yandexId: String, emails: List<String>) = query {
        val emailsStr = if (emails.isNotEmpty()) emails.joinToString(",").lowercase() else null
        log.info("[SHARE_DEBUG] ensureUser: yandexId=$yandexId, emailsToStore=$emailsStr")
        val existing = Users.selectAll().where { Users.yandexId eq yandexId }.firstOrNull()
        if (existing == null) {
            log.info("[SHARE_DEBUG] ensureUser: creating new user with email=$emailsStr")
            Users.insert { 
                it[Users.yandexId] = yandexId 
                it[Users.email] = emailsStr
            }
        } else if (emailsStr != null && existing[Users.email] != emailsStr) {
            log.info("[SHARE_DEBUG] ensureUser: updating email from '${existing[Users.email]}' to '$emailsStr'")
            Users.update({ Users.yandexId eq yandexId }) {
                it[Users.email] = emailsStr
            }
        } else {
            log.info("[SHARE_DEBUG] ensureUser: email unchanged '${existing[Users.email]}'")
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
