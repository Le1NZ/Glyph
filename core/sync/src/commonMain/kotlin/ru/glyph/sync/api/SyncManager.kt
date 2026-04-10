package ru.glyph.sync.api

interface SyncManager {
    /**
     * Скачивает все заметки с сервера и мёрджит с локальной БД.
     * Сервер является источником истины для конфликтов.
     */
    suspend fun pullAll(): Result<Unit>

    /**
     * Отправляет все локальные заметки на сервер.
     */
    suspend fun pushAll(): Result<Unit>
}
