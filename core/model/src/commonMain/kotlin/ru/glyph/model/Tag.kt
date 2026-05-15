package ru.glyph.model

data class Tag(
    val id: String,
    val name: String,
    val color: FolderColor,
    val createdAt: Long,
    val updatedAt: Long,
)
