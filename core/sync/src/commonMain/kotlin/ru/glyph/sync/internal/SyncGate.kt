package ru.glyph.sync.internal

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Shared "are we currently pulling from the server" gate. Both [SyncObserver]
 * and [FolderSyncObserver] suspend their push diff loops while this is true,
 * so a server-authoritative pull doesn't race with local push diffs.
 */
internal class SyncGate {
    val isSyncing = MutableStateFlow(false)
}
