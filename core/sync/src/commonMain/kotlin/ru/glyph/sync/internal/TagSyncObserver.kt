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
import ru.glyph.database.api.TagsRepository
import ru.glyph.model.FolderColor
import ru.glyph.model.Tag
import ru.glyph.sync.internal.network.TagApiService
import ru.glyph.utils.flow.collectLatestIn
import ru.glyph.utils.flow.windowedWithPrevious

@OptIn(ExperimentalCoroutinesApi::class)
internal class TagSyncObserver(
    private val tagsRepository: TagsRepository,
    private val apiService: TagApiService,
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

    suspend fun pullAll() {
        if (userCenter.authState.value != UserState.Authorized) return
        val remoteTags = apiService.getAll()
        tagsRepository.deleteAll()
        remoteTags.forEach { dto ->
            tagsRepository.upsert(
                tag = Tag(
                    id = dto.id,
                    name = dto.name,
                    color = FolderColor.fromKey(dto.color),
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
                tagsRepository
                    .observeAll()
                    .distinctUntilChanged()
                    .windowedWithPrevious(emptyList())
                    .drop(1)
            }
        }.collect { (prev, curr) -> syncChanges(prev, curr) }
    }

    private suspend fun syncChanges(prev: List<Tag>, curr: List<Tag>) {
        val prevIds = prev.map { it.id }.toSet()
        val prevById = prev.associateBy { it.id }

        (prevIds - curr.map { it.id }.toSet()).forEach { id ->
            runCatching { apiService.delete(id) }
        }

        curr.filter { it.id !in prevIds }.forEach { tag ->
            runCatching {
                val serverTag = apiService.create(
                    id = tag.id,
                    name = tag.name,
                    color = tag.color.name,
                    createdAt = tag.createdAt,
                    updatedAt = tag.updatedAt,
                )
                applyServerResponse(pushed = tag, serverTag = serverTag)
            }
        }

        curr.filter { currTag ->
            val previous = prevById[currTag.id]
            previous != null && previous.updatedAt != currTag.updatedAt
        }.forEach { tag ->
            runCatching {
                val serverTag = apiService.update(
                    id = tag.id,
                    name = tag.name,
                    color = tag.color.name,
                    updatedAt = tag.updatedAt,
                )
                applyServerResponse(pushed = tag, serverTag = serverTag)
            }
        }
    }

    private suspend fun applyServerResponse(
        pushed: Tag,
        serverTag: ru.glyph.sync.internal.network.dto.TagDto,
    ) {
        val incoming = Tag(
            id = serverTag.id,
            name = serverTag.name,
            color = FolderColor.fromKey(serverTag.color),
            createdAt = serverTag.createdAt,
            updatedAt = serverTag.updatedAt,
        )
        val currentLocal = tagsRepository.getById(pushed.id) ?: return
        val localUntouchedSinceWeSent = currentLocal.updatedAt == pushed.updatedAt
        val serverDiffersFromLocal = currentLocal != incoming
        if (localUntouchedSinceWeSent && serverDiffersFromLocal) {
            tagsRepository.upsert(incoming)
        }
    }
}
