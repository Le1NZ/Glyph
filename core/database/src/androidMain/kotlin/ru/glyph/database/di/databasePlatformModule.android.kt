package ru.glyph.database.di

import android.content.Context
import androidx.room.Room
import org.koin.dsl.module
import ru.glyph.database.internal.GlyphDatabase

internal actual fun databasePlatformModule() = module {
    single<GlyphDatabase> {
        val context: Context = get()
        Room.databaseBuilder<GlyphDatabase>(
            context = context,
            name = context.getDatabasePath("glyph.db").absolutePath,
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }
}
