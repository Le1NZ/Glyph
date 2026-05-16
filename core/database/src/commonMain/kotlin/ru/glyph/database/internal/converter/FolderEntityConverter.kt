package ru.glyph.database.internal.converter

import ru.glyph.database.internal.entity.FolderEntity
import ru.glyph.model.Folder
import ru.glyph.model.FolderColor
import ru.glyph.model.FolderPermission

internal fun Folder.toEntity() = FolderEntity(
    id = id,
    name = name,
    color = color.name,
    parentFolderId = parentFolderId,
    permission = permission.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun FolderEntity.toDomain() = Folder(
    id = id,
    name = name,
    color = FolderColor.fromKey(color),
    parentFolderId = parentFolderId,
    permission = FolderPermission.valueOf(permission),
    createdAt = createdAt,
    updatedAt = updatedAt,
)
