package ru.glyph.server.database

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val yandexId = varchar("yandex_id", 255)
    override val primaryKey = PrimaryKey(yandexId)
}

object Notes : Table("notes") {
    val id = varchar("id", 36)
    val userYandexId = varchar("user_yandex_id", 255).references(Users.yandexId)
    val title = text("title")
    val content = text("content")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}
