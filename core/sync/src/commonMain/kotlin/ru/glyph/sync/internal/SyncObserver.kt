package ru.glyph.sync.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.UserState
import ru.glyph.database.api.NotesRepository
import ru.glyph.database.api.entity.NoteEntity
import ru.glyph.sync.api.SyncBootstrap
import ru.glyph.sync.internal.network.NoteApiService
import ru.glyph.utils.flow.collectLatestIn
import ru.glyph.utils.flow.windowedWithPrevious

@OptIn(ExperimentalCoroutinesApi::class)
internal class SyncObserver(
    private val notesRepository: NotesRepository,
    private val apiService: NoteApiService,
    private val userCenter: UserCenter,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : SyncBootstrap {

    private var pullAsyncJob: Job? = null

    private val isSyncing = MutableStateFlow(false)

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

    override suspend fun pullAll(): Result<Unit> = runCatching {
        if (userCenter.authState.value != UserState.Authorized) return@runCatching

        isSyncing.value = true
        try {
            val remoteNotes = apiService.getAll()
            notesRepository.deleteAll()
            remoteNotes.forEach { dto ->
                notesRepository.upsert(
                    id = dto.id,
                    title = dto.title,
                    content = dto.content,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt,
                )
            }
        } finally {
            isSyncing.value = false
        }
    }

    private suspend fun observeDbAndPush() {
        isSyncing
            .flatMapLatest { syncing ->
                if (syncing) emptyFlow()
                else notesRepository.observeAll().windowedWithPrevious(emptyList()).drop(1)
            }
            .collect { (prev, curr) -> syncChanges(prev, curr) }
    }

    private suspend fun syncChanges(prev: List<NoteEntity>, curr: List<NoteEntity>) {
        val prevIds = prev.map { it.id }.toSet()
        val prevById = prev.associateBy { it.id }

        (prevIds - curr.map { it.id }.toSet()).forEach { id ->
            runCatching { apiService.delete(id) }
        }

        curr.filter { it.id !in prevIds }.forEach { note ->
            runCatching {
                val serverNote = apiService.create(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt,
                )

                notesRepository.upsert(
                    id = serverNote.id,
                    title = serverNote.title,
                    content = serverNote.content,
                    createdAt = serverNote.createdAt,
                    updatedAt = serverNote.updatedAt,
                )
            }
        }

        curr.filter { currNote ->
            val p = prevById[currNote.id]
            p != null && p.updatedAt != currNote.updatedAt
        }.forEach { note ->
            runCatching {
                val serverNote = apiService.update(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    updatedAt = note.updatedAt,
                )

                notesRepository.upsert(
                    id = serverNote.id,
                    title = serverNote.title,
                    content = serverNote.content,
                    createdAt = serverNote.createdAt,
                    updatedAt = serverNote.updatedAt,
                )
            }
        }
    }
}
