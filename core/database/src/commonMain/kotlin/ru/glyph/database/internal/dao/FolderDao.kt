package ru.glyph.database.internal.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.glyph.database.internal.entity.FolderEntity

@Dao
internal interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentFolderId IS NULL ORDER BY createdAt ASC")
    fun observeRoot(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentFolderId = :parentId ORDER BY createdAt ASC")
    fun observeByParent(parentId: String): Flow<List<FolderEntity>>

    @Query("SELECT parentFolderId, COUNT(*) AS count FROM folders WHERE parentFolderId IS NOT NULL GROUP BY parentFolderId")
    fun observeSubfolderCounts(): Flow<List<SubfolderCount>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: String): FolderEntity?

    @Upsert
    suspend fun upsert(folder: FolderEntity)

    /**
     * Detach members (notes' folderId → null, child folders' parentFolderId → null)
     * before deleting the folder. The diff loop in SyncObserver/FolderSyncObserver
     * will push the resulting note/folder updates ahead of the folder DELETE.
     */
    @androidx.room.Transaction
    suspend fun deleteWithCascadeDetach(id: String, updatedAt: Long) {
        detachNotesFrom(id, updatedAt)
        detachChildFoldersFrom(id, updatedAt)
        deleteById(id)
    }

    @Query("UPDATE notes SET folderId = NULL, updatedAt = :updatedAt WHERE folderId = :folderId")
    suspend fun detachNotesFrom(folderId: String, updatedAt: Long)

    @Query("UPDATE folders SET parentFolderId = NULL, updatedAt = :updatedAt WHERE parentFolderId = :folderId")
    suspend fun detachChildFoldersFrom(folderId: String, updatedAt: Long)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM folders")
    suspend fun deleteAll()
}

internal data class SubfolderCount(
    val parentFolderId: String,
    val count: Int,
)
