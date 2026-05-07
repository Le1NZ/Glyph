package ru.glyph.sync.api.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.glyph.sync.api.SyncBootstrap
import ru.glyph.sync.internal.FolderSyncObserver
import ru.glyph.sync.internal.SyncGate
import ru.glyph.sync.internal.SyncObserver
import ru.glyph.sync.internal.network.FolderApiService
import ru.glyph.sync.internal.network.FolderApiServiceImpl
import ru.glyph.sync.internal.network.NoteApiService
import ru.glyph.sync.internal.network.NoteApiServiceImpl

object SyncLocalDi {
    val module = module {
        single<NoteApiService> { NoteApiServiceImpl(get(), get()) }
        single<FolderApiService> { FolderApiServiceImpl(get(), get()) }
        single { SyncGate() }
        single(createdAtStart = true) {
            FolderSyncObserver(
                foldersRepository = get(),
                apiService = get(),
                userCenter = get(),
                syncGate = get(),
            )
        }
        single(createdAtStart = true) {
            SyncObserver(
                notesRepository = get(),
                apiService = get(),
                userCenter = get(),
                folderSyncObserver = get(),
                syncGate = get(),
            )
        } bind SyncBootstrap::class
    }
}
