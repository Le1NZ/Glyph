package ru.glyph.server.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val yandexId = varchar("yandex_id", 255)
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
