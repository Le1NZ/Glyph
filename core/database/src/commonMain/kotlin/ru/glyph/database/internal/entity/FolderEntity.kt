package ru.glyph.database.internal.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
internal data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val parentFolderId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
