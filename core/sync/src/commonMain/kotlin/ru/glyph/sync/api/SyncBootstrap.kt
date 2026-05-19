package ru.glyph.sync.api

interface SyncBootstrap {
    suspend fun pullAll(): Result<Unit>
    fun pullAsync()
    suspend fun pullNote(id: String): Result<Unit>
}
