package ru.glyph.database.internal.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
internal data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val createdAt: Long,
    val updatedAt: Long,
)
