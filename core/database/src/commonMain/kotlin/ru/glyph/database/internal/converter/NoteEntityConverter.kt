package ru.glyph.database.internal.converter

import ru.glyph.database.internal.entity.NoteEntity
import ru.glyph.model.Note

internal fun Note.toEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun NoteEntity.toDomain() = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
)