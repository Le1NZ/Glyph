package ru.glyph.database.internal.converter

import ru.glyph.database.internal.entity.NoteEntity
import ru.glyph.model.Note

import ru.glyph.model.NotePermission

internal fun Note.toEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    folderId = folderId,
    tagIds = tagIds,
    permission = permission.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun NoteEntity.toDomain() = Note(
    id = id,
    title = title,
    content = content,
    folderId = folderId,
    tagIds = tagIds,
    permission = NotePermission.valueOf(permission),
    createdAt = createdAt,
    updatedAt = updatedAt,
)