package ru.glyph.sync.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.UserState
import ru.glyph.database.api.FoldersRepository
import ru.glyph.model.Folder
import ru.glyph.model.FolderColor
import ru.glyph.sync.internal.network.FolderApiService
import ru.glyph.sync.internal.network.dto.FolderDto
import ru.glyph.utils.flow.collectLatestIn
import ru.glyph.utils.flow.windowedWithPrevious

@OptIn(ExperimentalCoroutinesApi::class)
internal class FolderSyncObserver(
    private val foldersRepository: FoldersRepository,
    private val apiService: FolderApiService,
    private val userCenter: UserCenter,
    private val syncGate: SyncGate,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {

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

    /**
     * Server-authoritative replace. Caller (SyncObserver.pullAll) must own the
     * isSyncing gate so the push diff loop is paused for the whole pull.
     */
    suspend fun pullAll() {
        if (userCenter.authState.value != UserState.Authorized) return
        val remoteFolders = apiService.getAll()
        foldersRepository.deleteAll()
        remoteFolders.forEach { dto ->
            foldersRepository.upsert(
                folder = Folder(
                    id = dto.id,
                    name = dto.name,
                    color = FolderColor.fromKey(dto.color),
                    parentFolderId = dto.parentFolderId,
                    permission = dto.permission,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt,
                ),
            )
        }
    }

    private suspend fun observeDbAndPush() {
        syncGate.isSyncing.flatMapLatest { syncing ->
            if (syncing) {
                emptyFlow()
            } else {
                foldersRepository
                    .observeAll()
                    .distinctUntilChanged()
                    .windowedWithPrevious(emptyList())
                    .drop(1)
            }
        }.collect { (prev, curr) -> syncChanges(prev, curr) }
    }

    private suspend fun syncChanges(prev: List<Folder>, curr: List<Folder>) {
        val prevIds = prev.map { it.id }.toSet()
        val prevById = prev.associateBy { it.id }

        (prevIds - curr.map { it.id }.toSet()).forEach { id ->
            runCatching { apiService.delete(id) }
        }

        curr.filter { it.id !in prevIds }.forEach { folder ->
            runCatching {
                val serverFolder = apiService.create(
                    id = folder.id,
                    name = folder.name,
                    color = folder.color.name,
                    parentFolderId = folder.parentFolderId,
                    createdAt = folder.createdAt,
                    updatedAt = folder.updatedAt,
                )
                applyServerResponse(pushed = folder, serverFolder = serverFolder)
            }
        }

        curr.filter { currFolder ->
            val previous = prevById[currFolder.id]
            previous != null && previous.updatedAt != currFolder.updatedAt
        }.forEach { folder ->
            runCatching {
                val serverFolder = apiService.update(
                    id = folder.id,
                    name = folder.name,
                    color = folder.color.name,
                    parentFolderId = folder.parentFolderId,
                    updatedAt = folder.updatedAt,
                )
                applyServerResponse(pushed = folder, serverFolder = serverFolder)
            }
        }
    }

    private suspend fun applyServerResponse(
        pushed: Folder,
        serverFolder: FolderDto,
    ) {
        val incoming = Folder(
            id = serverFolder.id,
            name = serverFolder.name,
            color = FolderColor.fromKey(serverFolder.color),
            parentFolderId = serverFolder.parentFolderId,
            permission = serverFolder.permission,
            createdAt = serverFolder.createdAt,
            updatedAt = serverFolder.updatedAt,
        )
        val currentLocal = foldersRepository.getById(pushed.id) ?: return
        val localUntouchedSinceWeSent = currentLocal.updatedAt == pushed.updatedAt
        val serverDiffersFromLocal = currentLocal != incoming
        if (localUntouchedSinceWeSent && serverDiffersFromLocal) {
            foldersRepository.upsert(incoming)
        }
    }
}
