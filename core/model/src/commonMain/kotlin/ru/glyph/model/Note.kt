package ru.glyph.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val folderId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
