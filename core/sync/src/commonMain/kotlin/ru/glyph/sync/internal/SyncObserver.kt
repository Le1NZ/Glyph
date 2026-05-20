package ru.glyph.sync.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.UserState
import ru.glyph.database.api.NotesRepository
import ru.glyph.model.Note
import ru.glyph.model.NotePermission
import ru.glyph.sync.api.SyncBootstrap
import ru.glyph.sync.internal.network.NoteApiService
import ru.glyph.sync.internal.network.dto.NoteDto
import ru.glyph.utils.flow.collectLatestIn
import ru.glyph.utils.flow.windowedWithPrevious

@OptIn(ExperimentalCoroutinesApi::class)
internal class SyncObserver(
    private val notesRepository: NotesRepository,
    private val apiService: NoteApiService,
    private val userCenter: UserCenter,
    private val folderSyncObserver: FolderSyncObserver,
    private val tagSyncObserver: TagSyncObserver,
    private val syncGate: SyncGate,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : SyncBootstrap {

    private var pullAsyncJob: Job? = null

    init {
        userCenter.authState.collectLatestIn(
            scope = scope,
        ) { state ->
            when (state) {
                UserState.Authorized -> observeDbAndPush()
                UserState.NotAuthorized -> Unit
            }
        }
    }

    override fun pullAsync() {
        pullAsyncJob?.cancel()
        pullAsyncJob = scope.launch { pullAll() }
    }

    override suspend fun pullNote(id: String): Result<Unit> = runCatching {
        val dto = apiService.getById(id) ?: return@runCatching
        val local = notesRepository.getById(id)
        // Only upsert if the server has a strictly newer version,
        // so we never wipe in-progress local edits.
        if (local == null || dto.updatedAt > local.updatedAt) {
            notesRepository.upsert(
                Note(
                    id = dto.id,
                    title = dto.title,
                    content = dto.content,
                    folderId = dto.folderId,
                    tagIds = dto.tagIds,
                    permission = dto.permission,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt,
                )
            )
        }
    }

    override suspend fun pullAll(): Result<Unit> = runCatching {
        if (userCenter.authState.value != UserState.Authorized) return@runCatching

        syncGate.isSyncing.value = true
        try {
            // Folders first so freshly-pulled notes' folderId resolves locally.
            folderSyncObserver.pullAll()
            tagSyncObserver.pullAll()

            val remoteNotes = apiService.getAll()
            notesRepository.deleteAll()
            remoteNotes.forEach { dto ->
                notesRepository.upsert(
                    note = Note(
                        id = dto.id,
                        title = dto.title,
                        content = dto.content,
                        folderId = dto.folderId,
                        tagIds = dto.tagIds,
                        permission = dto.permission,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                    )
                )
            }
        } finally {
            syncGate.isSyncing.value = false
        }
    }

    private suspend fun observeDbAndPush() {
        syncGate.isSyncing.flatMapLatest { syncing ->
            if (syncing) {
                emptyFlow()
            } else {
                notesRepository
                    .observeAll()
                    .distinctUntilChanged()
                    .windowedWithPrevious(emptyList())
                    .drop(1)
            }
        }.collect { (prev, curr) -> syncChanges(prev, curr) }
    }

    private suspend fun syncChanges(prev: List<Note>, curr: List<Note>) {
        // Only sync notes the user owns. READ notes are server-controlled (shared with us).
        val prevOwned = prev.filter { it.permission == NotePermission.WRITE }
        val currOwned = curr.filter { it.permission == NotePermission.WRITE }

        val prevOwnedIds = prevOwned.map { it.id }.toSet()
        val prevOwnedById = prevOwned.associateBy { it.id }
        val currOwnedIds = currOwned.map { it.id }.toSet()

        (prevOwnedIds - currOwnedIds).forEach { id ->
            runCatching { apiService.delete(id) }
        }

        currOwned.filter { it.id !in prevOwnedIds }.forEach { note ->
            runCatching {
                val serverNote = apiService.create(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = note.folderId,
                    tagIds = note.tagIds,
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt,
                )
                applyServerResponse(pushed = note, serverNote = serverNote)
            }
        }

        currOwned.filter { currNote ->
            val previous = prevOwnedById[currNote.id]
            previous != null && previous.updatedAt != currNote.updatedAt
        }.forEach { note ->
            runCatching {
                val serverNote = apiService.update(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = note.folderId,
                    tagIds = note.tagIds,
                    updatedAt = note.updatedAt,
                )
                applyServerResponse(pushed = note, serverNote = serverNote)
            }
        }
    }

    private suspend fun applyServerResponse(
        pushed: Note,
        serverNote: NoteDto
    ) {
        val incoming = Note(
            id = serverNote.id,
            title = serverNote.title,
            content = serverNote.content,
            folderId = serverNote.folderId,
            tagIds = serverNote.tagIds,
            permission = serverNote.permission,
            createdAt = serverNote.createdAt,
            updatedAt = serverNote.updatedAt,
        )
        val currentLocal = notesRepository.getById(pushed.id) ?: return
        val localUntouchedSinceWeSent = currentLocal.updatedAt == pushed.updatedAt
        val serverDiffersFromLocal = currentLocal != incoming
        if (localUntouchedSinceWeSent && serverDiffersFromLocal) {
            notesRepository.upsert(incoming)
        }
    }
}
