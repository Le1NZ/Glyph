package ru.glyph.database.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.glyph.database.api.FoldersRepository
import ru.glyph.database.internal.converter.toDomain
import ru.glyph.database.internal.converter.toEntity
import ru.glyph.database.internal.dao.FolderDao
import ru.glyph.database.internal.entity.FolderEntity
import ru.glyph.model.Folder
import ru.glyph.model.FolderColor
import ru.glyph.utils.clock.currentTimeDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

import ru.glyph.model.FolderPermission

@OptIn(ExperimentalUuidApi::class)
internal class FoldersRepositoryImpl(
    private val dao: FolderDao,
) : FoldersRepository {

    override fun observeAll(): Flow<List<Folder>> {
        return dao.observeAll().map { folders -> folders.map(FolderEntity::toDomain) }
    }

    override fun observeByParent(parentFolderId: String?): Flow<List<Folder>> {
        val source = if (parentFolderId == null) dao.observeRoot() else dao.observeByParent(parentFolderId)
        return source.map { folders -> folders.map(FolderEntity::toDomain) }
    }

    override fun observeSubfolderCounts(): Flow<Map<String, Int>> {
        return dao.observeSubfolderCounts().map { rows ->
            rows.associate { it.parentFolderId to it.count }
        }
    }

    override suspend fun getById(id: String): Folder? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun create(name: String, parentFolderId: String?): String {
        val id = Uuid.random().toString()
        val now = currentTimeDuration().inWholeMilliseconds
        val existingCount = dao.observeAll().first().size
        val color = FolderColor.byIndex(existingCount)

        val folder = FolderEntity(
            id = id,
            name = name,
            color = color.name,
            parentFolderId = parentFolderId,
            permission = FolderPermission.WRITE.name,
            createdAt = now,
            updatedAt = now,
        )

        dao.upsert(folder)
        return id
    }

    override suspend fun upsert(folder: Folder) {
        dao.upsert(folder.toEntity())
    }

    override suspend fun rename(id: String, name: String) {
        val existing = dao.getById(id) ?: return
        dao.upsert(
            existing.copy(
                name = name,
                updatedAt = currentTimeDuration().inWholeMilliseconds,
            ),
        )
    }

    override suspend fun setColor(id: String, color: FolderColor) {
        val existing = dao.getById(id) ?: return
        dao.upsert(
            existing.copy(
                color = color.name,
                updatedAt = currentTimeDuration().inWholeMilliseconds,
            ),
        )
    }

    override suspend fun delete(id: String) {
        dao.deleteWithCascadeDetach(id, currentTimeDuration().inWholeMilliseconds)
    }

    override suspend fun deleteAll() = dao.deleteAll()
}
