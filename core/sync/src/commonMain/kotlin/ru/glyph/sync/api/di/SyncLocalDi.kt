package ru.glyph.sync.api.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.glyph.sync.api.SyncBootstrap
import ru.glyph.sync.internal.SyncObserver
import ru.glyph.sync.internal.network.NoteApiService

object SyncLocalDi {
    val module = module {
        single { NoteApiService(get(), get()) }
        single(createdAtStart = true) {
            SyncObserver(
                notesRepository = get(),
                apiService = get(),
                userCenter = get(),
            )
        } bind SyncBootstrap::class
    }
}
