package ru.glyph.database.api

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import ru.glyph.database.api.dao.NoteDao
import ru.glyph.database.api.entity.NoteEntity

@Database(entities = [NoteEntity::class], version = 1)
@ConstructedBy(GlyphDatabaseConstructor::class)
abstract class GlyphDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object GlyphDatabaseConstructor : RoomDatabaseConstructor<GlyphDatabase> {
    override fun initialize(): GlyphDatabase
}
