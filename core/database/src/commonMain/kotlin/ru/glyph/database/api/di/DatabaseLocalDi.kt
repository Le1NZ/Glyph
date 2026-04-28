package ru.glyph.database.api.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.glyph.database.internal.GlyphDatabase
import ru.glyph.database.api.NotesRepository
import ru.glyph.database.di.databasePlatformModule
import ru.glyph.database.internal.NotesRepositoryImpl

object DatabaseLocalDi {
    val module: Module = module {
        includes(databasePlatformModule())
        single<NotesRepository> { NotesRepositoryImpl(get<GlyphDatabase>().noteDao()) }
    }
}
