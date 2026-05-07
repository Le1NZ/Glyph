package ru.glyph.model

data class Folder(
    val id: String,
    val name: String,
    val color: FolderColor,
    val parentFolderId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
