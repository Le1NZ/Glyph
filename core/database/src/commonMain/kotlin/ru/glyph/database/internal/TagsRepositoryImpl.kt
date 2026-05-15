package ru.glyph.database.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.glyph.database.api.TagsRepository
import ru.glyph.database.internal.converter.toDomain
import ru.glyph.database.internal.converter.toEntity
import ru.glyph.database.internal.dao.TagDao
import ru.glyph.model.Tag

internal class TagsRepositoryImpl(
    private val tagDao: TagDao,
) : TagsRepository {

    override fun observeAll(): Flow<List<Tag>> {
        return tagDao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: String): Tag? {
        return tagDao.getById(id)?.toDomain()
    }

    override suspend fun upsert(tag: Tag) {
        tagDao.upsert(tag.toEntity())
    }

    override suspend fun deleteById(id: String) {
        tagDao.deleteById(id)
    }

    override suspend fun deleteAll() {
        tagDao.deleteAll()
    }
}
