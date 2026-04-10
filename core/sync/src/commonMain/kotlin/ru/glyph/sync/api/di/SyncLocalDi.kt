package ru.glyph.sync.api.di

import org.koin.dsl.module
import ru.glyph.sync.api.SyncManager
import ru.glyph.sync.internal.SyncManagerImpl
import ru.glyph.sync.internal.network.NoteApiService

object SyncLocalDi {
    val module = module {
        single { NoteApiService(get()) }
        single<SyncManager> { SyncManagerImpl(get()) }
    }
}
