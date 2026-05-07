package ru.glyph.database.internal.converter

import ru.glyph.database.internal.entity.FolderEntity
import ru.glyph.model.Folder
import ru.glyph.model.FolderColor

internal fun Folder.toEntity() = FolderEntity(
    id = id,
    name = name,
    color = color.name,
    parentFolderId = parentFolderId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun FolderEntity.toDomain() = Folder(
    id = id,
    name = name,
    color = FolderColor.fromKey(color),
    parentFolderId = parentFolderId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
