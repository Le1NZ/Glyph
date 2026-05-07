package ru.glyph.database.api

import kotlinx.coroutines.flow.Flow
import ru.glyph.model.Folder
import ru.glyph.model.FolderColor

interface FoldersRepository {

    fun observeAll(): Flow<List<Folder>>

    /** parentFolderId == null returns root folders. */
    fun observeByParent(parentFolderId: String?): Flow<List<Folder>>

    /** Map of parentFolderId -> count of subfolders inside it. */
    fun observeSubfolderCounts(): Flow<Map<String, Int>>

    suspend fun getById(id: String): Folder?

    /**
     * @return id of created folder. Color is auto-picked from the palette by current count.
     */
    suspend fun create(name: String, parentFolderId: String? = null): String

    suspend fun upsert(folder: Folder)

    suspend fun rename(id: String, name: String)

    suspend fun setColor(id: String, color: FolderColor)

    /**
     * Detaches member notes and child folders before deleting the folder.
     */
    suspend fun delete(id: String)

    suspend fun deleteAll()
}
