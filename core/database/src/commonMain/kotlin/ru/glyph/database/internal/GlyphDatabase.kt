package ru.glyph.database.internal

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import ru.glyph.database.internal.dao.FolderDao
import ru.glyph.database.internal.dao.NoteDao
import ru.glyph.database.internal.entity.FolderEntity
import ru.glyph.database.internal.entity.NoteEntity

@Database(entities = [NoteEntity::class, FolderEntity::class], version = 1)
@ConstructedBy(GlyphDatabaseConstructor::class)
internal abstract class GlyphDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object GlyphDatabaseConstructor : RoomDatabaseConstructor<GlyphDatabase> {
    override fun initialize(): GlyphDatabase
}
