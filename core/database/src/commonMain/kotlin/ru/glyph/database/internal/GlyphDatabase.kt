package ru.glyph.database.internal

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import ru.glyph.database.internal.converter.StringListConverter
import ru.glyph.database.internal.dao.FolderDao
import ru.glyph.database.internal.dao.NoteDao
import ru.glyph.database.internal.dao.TagDao
import ru.glyph.database.internal.entity.FolderEntity
import ru.glyph.database.internal.entity.NoteEntity
import ru.glyph.database.internal.entity.TagEntity

@Database(entities = [NoteEntity::class, FolderEntity::class, TagEntity::class], version = 1)
@TypeConverters(StringListConverter::class)
@ConstructedBy(GlyphDatabaseConstructor::class)
internal abstract class GlyphDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object GlyphDatabaseConstructor : RoomDatabaseConstructor<GlyphDatabase> {
    override fun initialize(): GlyphDatabase
}
