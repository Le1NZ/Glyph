package ru.glyph.sync.api

interface SyncBootstrap {
    suspend fun pullAll(): Result<Unit>
    fun pullAsync()
}
