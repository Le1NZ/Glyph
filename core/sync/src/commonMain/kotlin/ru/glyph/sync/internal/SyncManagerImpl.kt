package ru.glyph.sync.internal

import ru.glyph.sync.api.SyncManager
import ru.glyph.sync.internal.network.NoteApiService

// TODO: реализовать полноценную синхронизацию после подключения бэкенда
internal class SyncManagerImpl(
    private val apiService: NoteApiService,
) : SyncManager {

    override suspend fun pullAll(): Result<Unit> = Result.success(Unit)

    override suspend fun pushAll(): Result<Unit> = Result.success(Unit)
}
