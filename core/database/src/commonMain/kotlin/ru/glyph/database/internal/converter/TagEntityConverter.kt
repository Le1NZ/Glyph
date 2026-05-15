package ru.glyph.database.internal.converter

import ru.glyph.database.internal.entity.TagEntity
import ru.glyph.model.FolderColor
import ru.glyph.model.Tag

internal fun Tag.toEntity() = TagEntity(
    id = id,
    name = name,
    color = color.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun TagEntity.toDomain() = Tag(
    id = id,
    name = name,
    color = FolderColor.fromKey(color),
    createdAt = createdAt,
    updatedAt = updatedAt,
)
