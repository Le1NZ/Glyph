package ru.glyph.server.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val yandexId = varchar("yandex_id", 255)
    val email = varchar("email", 255).nullable()
    override val primaryKey = PrimaryKey(yandexId)
}

object Folders : Table("folders") {
    val id = varchar("id", 36)
    val userYandexId = varchar("user_yandex_id", 255).references(Users.yandexId)
    val name = text("name")
    val color = varchar("color", 16)
    val parentFolderId = varchar("parent_folder_id", 36)
        .references(id, onDelete = ReferenceOption.SET_NULL)
        .nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object Notes : Table("notes") {
    val id = varchar("id", 36)
    val userYandexId = varchar("user_yandex_id", 255).references(Users.yandexId)
    val folderId = varchar("folder_id", 36)
        .references(Folders.id, onDelete = ReferenceOption.SET_NULL)
        .nullable()
    val title = text("title").default("")
    val content = text("content")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object Tags : Table("tags") {
    val id = varchar("id", 36)
    val userYandexId = varchar("user_yandex_id", 255).references(Users.yandexId)
    val name = text("name")
    val color = varchar("color", 16)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object NoteTags : Table("note_tags") {
    val noteId = varchar("note_id", 36).references(Notes.id, onDelete = ReferenceOption.CASCADE)
    val tagId = varchar("tag_id", 36).references(Tags.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(noteId, tagId)
}

object NoteShares : Table("note_shares") {
    val noteId = varchar("note_id", 36).references(Notes.id, onDelete = ReferenceOption.CASCADE)
    /** Resolved Yandex user ID of the recipient – used for reliable matching. */
    val yandexId = varchar("yandex_id", 255).references(Users.yandexId, onDelete = ReferenceOption.CASCADE)
    /** Email that the owner typed when sharing – kept only for display purposes. */
    val displayEmail = varchar("display_email", 255)
    val permission = varchar("permission", 16) // "READ" or "WRITE"
    override val primaryKey = PrimaryKey(noteId, yandexId)
}
