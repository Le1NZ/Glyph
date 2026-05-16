package ru.glyph.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.SignInResult
import ru.glyph.auth.api.model.UserState
import ru.glyph.database.api.FoldersRepository
import ru.glyph.database.api.NotesRepository
import ru.glyph.model.Folder
import ru.glyph.model.FolderColor
import ru.glyph.model.FolderPermission
import ru.glyph.model.Note
import ru.glyph.model.NotePermission
import ru.glyph.sync.internal.FolderSyncObserver
import ru.glyph.sync.internal.SyncGate
import ru.glyph.sync.internal.SyncObserver
import ru.glyph.sync.internal.TagSyncObserver
import ru.glyph.sync.internal.network.FolderApiService
import ru.glyph.sync.internal.network.NoteApiService
import ru.glyph.sync.internal.network.dto.FolderDto
import ru.glyph.sync.internal.network.dto.NoteDto
import ru.glyph.sync.internal.network.dto.TagDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncObserverTest {

    // ─── Fakes ───────────────────────────────────────────────────────────────

    private class FakeNotesRepository : NotesRepository {
        private val _notes = MutableStateFlow<List<Note>>(emptyList())

        val upsertCalls = mutableListOf<Note>()
        var deleteAllCallCount = 0

        override fun observeAll(): Flow<List<Note>> = _notes.asStateFlow()

        override fun observeByFolder(folderId: String?): Flow<List<Note>> = _notes.asStateFlow()

        override fun observeFolderCounts(): Flow<Map<String, Int>> = MutableStateFlow(emptyMap())

        override fun search(query: String): Flow<List<Note>> = _notes.asStateFlow()

        override suspend fun getById(id: String) = _notes.value.find { it.id == id }

        override suspend fun create(title: String, content: String, folderId: String?): String {
            val id = "gen-${_notes.value.size}"
            _notes.value += Note(
                id = id,
                title = title,
                content = content,
                folderId = folderId,
                tagIds = emptyList(),
                permission = NotePermission.WRITE,
                createdAt = 0L,
                updatedAt = 0L
            )
            return id
        }

        override suspend fun upsert(note: Note) {
            upsertCalls.add(note)
            val list = _notes.value.toMutableList()
            val idx = list.indexOfFirst { it.id == note.id }
            if (idx >= 0) list[idx] = note else list.add(note)
            _notes.value = list
        }

        override suspend fun update(id: String, title: String, content: String) {
            val list = _notes.value.toMutableList()
            val idx = list.indexOfFirst { it.id == id }
            if (idx >= 0) list[idx] = list[idx].copy(title = title, content = content)
            _notes.value = list
        }

        override suspend fun setFolder(id: String, folderId: String?) {
            val list = _notes.value.toMutableList()
            val idx = list.indexOfFirst { it.id == id }
            if (idx >= 0) list[idx] = list[idx].copy(folderId = folderId)
            _notes.value = list
        }

        override suspend fun setTags(id: String, tagIds: List<String>) {
            val list = _notes.value.toMutableList()
            val idx = list.indexOfFirst { it.id == id }
            if (idx >= 0) list[idx] = list[idx].copy(tagIds = tagIds)
            _notes.value = list
        }

        override suspend fun delete(id: String) {
            _notes.value = _notes.value.filter { it.id != id }
        }

        override suspend fun deleteAll() {
            deleteAllCallCount++
            _notes.value = emptyList()
        }

        fun emit(notes: List<Note>) {
            _notes.value = notes
        }
    }

    private class FakeFoldersRepository : FoldersRepository {
        private val _folders = MutableStateFlow<List<Folder>>(emptyList())

        override fun observeAll(): Flow<List<Folder>> = _folders.asStateFlow()
        override fun observeByParent(parentFolderId: String?): Flow<List<Folder>> =
            _folders.asStateFlow()

        override fun observeSubfolderCounts(): Flow<Map<String, Int>> = MutableStateFlow(emptyMap())
        override suspend fun getById(id: String) = _folders.value.find { it.id == id }
        override suspend fun create(name: String, parentFolderId: String?): String {
            val id = "folder-${_folders.value.size}"
            _folders.value += Folder(
                id = id,
                name = name,
                color = FolderColor.BLUE,
                parentFolderId = parentFolderId,
                permission = FolderPermission.WRITE,
                createdAt = 0L,
                updatedAt = 0L,
            )
            return id
        }

        override suspend fun upsert(folder: Folder) {
            val list = _folders.value.toMutableList()
            val idx = list.indexOfFirst { it.id == folder.id }
            if (idx >= 0) list[idx] = folder else list.add(folder)
            _folders.value = list
        }

        override suspend fun rename(id: String, name: String) = Unit
        override suspend fun setColor(id: String, color: FolderColor) = Unit
        override suspend fun delete(id: String) {
            _folders.value = _folders.value.filter { it.id != id }
        }

        override suspend fun deleteAll() {
            _folders.value = emptyList()
        }
    }

    private class FakeNoteApiService : NoteApiService {
        var notesToReturn: List<NoteDto> = emptyList()
        var getAllDeferred: CompletableDeferred<Unit>? = null
        var getAllCallCount = 0

        val createCalls = mutableListOf<String>()
        val updateCalls = mutableListOf<String>()
        val deleteCalls = mutableListOf<String>()

        override suspend fun getAll(): List<NoteDto> {
            getAllCallCount++
            getAllDeferred?.await()
            return notesToReturn
        }

        override suspend fun create(
            id: String,
            title: String,
            content: String,
            folderId: String?,
            tagIds: List<String>,
            createdAt: Long,
            updatedAt: Long,
        ): NoteDto {
            createCalls.add(id)
            return NoteDto(
                id = id,
                title = title,
                content = content,
                folderId = folderId,
                tagIds = tagIds,
                permission = NotePermission.WRITE,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }

        override suspend fun update(
            id: String,
            title: String,
            content: String,
            folderId: String?,
            tagIds: List<String>,
            updatedAt: Long,
        ): NoteDto {
            updateCalls.add(id)
            return NoteDto(
                id = id,
                title = title,
                content = content,
                folderId = folderId,
                tagIds = tagIds,
                permission = NotePermission.WRITE,
                createdAt = 0L,
                updatedAt = updatedAt,
            )
        }

        override suspend fun delete(id: String) {
            deleteCalls.add(id)
        }
    }

    private class FakeFolderApiService : FolderApiService {
        override suspend fun getAll(): List<FolderDto> = emptyList()
        override suspend fun create(
            id: String,
            name: String,
            color: String,
            parentFolderId: String?,
            createdAt: Long,
            updatedAt: Long,
        ) = FolderDto(id, name, color, parentFolderId, FolderPermission.WRITE, createdAt, updatedAt)

        override suspend fun update(
            id: String,
            name: String,
            color: String,
            parentFolderId: String?,
            updatedAt: Long,
        ) = FolderDto(id, name, color, parentFolderId, FolderPermission.WRITE, 0L, updatedAt)

        override suspend fun delete(id: String) = Unit
    }

    private class FakeUserCenter(
        initialState: UserState = UserState.NotAuthorized,
    ) : UserCenter {
        override val authState = MutableStateFlow(initialState)
        override fun getToken() = if (authState.value == UserState.Authorized) "token" else null
        override suspend fun signIn() = SignInResult.Success
        override suspend fun signOut() {
            authState.value = UserState.NotAuthorized
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private class FakeTagApiService : ru.glyph.sync.internal.network.TagApiService {
        override suspend fun getAll() = emptyList<TagDto>()
        override suspend fun create(
            id: String,
            name: String,
            color: String,
            createdAt: Long,
            updatedAt: Long
        ) = TagDto(id, name, color, createdAt, updatedAt)

        override suspend fun update(id: String, name: String, color: String, updatedAt: Long) =
            TagDto(id, name, color, 0L, updatedAt)

        override suspend fun delete(id: String) {}
    }

    private class FakeTagsRepository : ru.glyph.database.api.TagsRepository {
        override fun observeAll() = MutableStateFlow(emptyList<ru.glyph.model.Tag>())
        override suspend fun getById(id: String) = null
        override suspend fun upsert(tag: ru.glyph.model.Tag) {}
        override suspend fun deleteById(id: String) {}
        override suspend fun deleteAll() {}
    }

    private fun observer(
        notesRepo: NotesRepository,
        apiService: NoteApiService,
        userCenter: UserCenter,
        scope: CoroutineScope,
    ): SyncObserver {
        val gate = SyncGate()
        val folderSync = FolderSyncObserver(
            foldersRepository = FakeFoldersRepository(),
            apiService = FakeFolderApiService(),
            userCenter = userCenter,
            syncGate = gate,
            scope = scope,
        )
        val tagSync = TagSyncObserver(
            tagsRepository = FakeTagsRepository(),
            apiService = FakeTagApiService(),
            userCenter = userCenter,
            syncGate = gate,
            scope = scope,
        )
        return SyncObserver(
            notesRepository = notesRepo,
            apiService = apiService,
            userCenter = userCenter,
            folderSyncObserver = folderSync,
            tagSyncObserver = tagSync,
            syncGate = gate,
            scope = scope,
        )
    }

    private fun note(id: String, updatedAt: Long = 0L) = Note(
        id = id,
        title = "Title $id",
        content = "Content $id",
        folderId = null,
        tagIds = emptyList(),
        permission = NotePermission.WRITE,
        createdAt = 0L,
        updatedAt = updatedAt,
    )

    private fun dto(id: String, updatedAt: Long = 0L) = NoteDto(
        id = id,
        title = "Title $id",
        content = "Content $id",
        folderId = null,
        tagIds = emptyList(),
        permission = NotePermission.WRITE,
        createdAt = 0L,
        updatedAt = updatedAt
    )

    // ─── pullAll tests ────────────────────────────────────────────────────────

    @Test
    fun `pullAll skips API call when not authorized`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.NotAuthorized)

        val observer = observer(notesRepo, apiService, userCenter, backgroundScope)
        observer.pullAll()

        assertEquals(0, apiService.getAllCallCount)
        assertEquals(0, notesRepo.deleteAllCallCount)
    }

    @Test
    fun `pullAll replaces DB contents with server data`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService().apply {
            notesToReturn = listOf(dto("id1", 100L), dto("id2", 200L))
        }
        val userCenter = FakeUserCenter(UserState.Authorized)

        val observer = observer(notesRepo, apiService, userCenter, backgroundScope)
        observer.pullAll()
        advanceUntilIdle()

        assertEquals(1, notesRepo.deleteAllCallCount)
        assertEquals(2, notesRepo.upsertCalls.size)
        assertEquals("id1", notesRepo.upsertCalls[0].id)
        assertEquals("id2", notesRepo.upsertCalls[1].id)
    }

    @Test
    fun `pullAll returns failure when API throws`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = object : NoteApiService {
            override suspend fun getAll(): List<NoteDto> = throw RuntimeException("network error")
            override suspend fun create(
                id: String,
                title: String,
                content: String,
                folderId: String?,
                tagIds: List<String>,
                createdAt: Long,
                updatedAt: Long,
            ) = NoteDto(
                id = id,
                title = title,
                content = content,
                folderId = folderId,
                tagIds = tagIds,
                permission = NotePermission.WRITE,
                createdAt = createdAt,
                updatedAt = updatedAt
            )

            override suspend fun update(
                id: String,
                title: String,
                content: String,
                folderId: String?,
                tagIds: List<String>,
                updatedAt: Long,
            ) = NoteDto(
                id = id,
                title = title,
                content = content,
                folderId = folderId,
                tagIds = tagIds,
                permission = NotePermission.WRITE,
                createdAt = 0L,
                updatedAt = updatedAt
            )

            override suspend fun delete(id: String) {}
        }

        val userCenter = FakeUserCenter(UserState.Authorized)

        val observer = observer(notesRepo, apiService, userCenter, backgroundScope)
        val result = observer.pullAll()

        assertTrue(result.isFailure)
        assertEquals(0, notesRepo.deleteAllCallCount)
    }

    @Test
    fun `DB changes during pullAll are not pushed to server`() =
        runTest(UnconfinedTestDispatcher()) {
            val notesRepo = FakeNotesRepository()
            val apiService = FakeNoteApiService()
            val userCenter = FakeUserCenter(UserState.Authorized)

            val observer = observer(notesRepo, apiService, userCenter, backgroundScope)
            advanceUntilIdle() // let observeDbAndPush start and consume initial emission

            val deferred = CompletableDeferred<Unit>()
            apiService.getAllDeferred = deferred

            backgroundScope.launch { observer.pullAll() }
            advanceUntilIdle() // pullAll is now suspended at getAll()

            // Emit a DB change while isSyncing = true
            notesRepo.emit(listOf(note("id1")))
            advanceUntilIdle()

            deferred.complete(Unit) // release pullAll
            advanceUntilIdle()

            assertTrue(apiService.createCalls.isEmpty(), "Create should not be called during sync")
            assertTrue(apiService.updateCalls.isEmpty())
            assertTrue(apiService.deleteCalls.isEmpty())
        }

    // ─── Observer tests ───────────────────────────────────────────────────────

    @Test
    fun `observer does not push when user is not authorized`() =
        runTest(UnconfinedTestDispatcher()) {
            val notesRepo = FakeNotesRepository()
            val apiService = FakeNoteApiService()
            val userCenter = FakeUserCenter(UserState.NotAuthorized)

            observer(notesRepo, apiService, userCenter, backgroundScope)
            advanceUntilIdle()

            notesRepo.emit(listOf(note("id1")))
            advanceUntilIdle()

            assertTrue(apiService.createCalls.isEmpty())
        }

    @Test
    fun `observer calls create API when new note added`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.Authorized)

        observer(notesRepo, apiService, userCenter, backgroundScope)
        advanceUntilIdle() // consume initial emission → drop(1)

        notesRepo.emit(listOf(note("id1")))
        advanceUntilIdle()

        assertEquals(listOf("id1"), apiService.createCalls)
        assertTrue(apiService.updateCalls.isEmpty())
        assertTrue(apiService.deleteCalls.isEmpty())
    }

    @Test
    fun `observer calls delete API when note removed`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.Authorized)

        // Pre-populate so the first emission (which gets dropped) includes the note
        notesRepo.emit(listOf(note("id1")))

        observer(notesRepo, apiService, userCenter, backgroundScope)
        advanceUntilIdle() // consumes [note1] as initial, drops it

        // Remove the note
        notesRepo.emit(emptyList())
        advanceUntilIdle()

        assertEquals(listOf("id1"), apiService.deleteCalls)
        assertTrue(apiService.createCalls.isEmpty())
    }

    @Test
    fun `observer calls update API when note updatedAt changes`() =
        runTest(UnconfinedTestDispatcher()) {
            val notesRepo = FakeNotesRepository()
            val apiService = FakeNoteApiService()
            val userCenter = FakeUserCenter(UserState.Authorized)

            notesRepo.emit(listOf(note("id1", updatedAt = 100L)))

            observer(notesRepo, apiService, userCenter, backgroundScope)
            advanceUntilIdle()

            notesRepo.emit(listOf(note("id1", updatedAt = 200L)))
            advanceUntilIdle()

            assertEquals(listOf("id1"), apiService.updateCalls)
            assertTrue(apiService.createCalls.isEmpty())
            assertTrue(apiService.deleteCalls.isEmpty())
        }

    @Test
    fun `observer stops pushing after user signs out`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.Authorized)

        observer(notesRepo, apiService, userCenter, backgroundScope)
        advanceUntilIdle()

        // Sign out
        userCenter.authState.value = UserState.NotAuthorized
        advanceUntilIdle()

        notesRepo.emit(listOf(note("id1")))
        advanceUntilIdle()

        assertTrue(apiService.createCalls.isEmpty(), "Should not push after sign out")
    }

    @Test
    fun `observer resumes pushing after user signs back in`() =
        runTest(UnconfinedTestDispatcher()) {
            val notesRepo = FakeNotesRepository()
            val apiService = FakeNoteApiService()
            val userCenter = FakeUserCenter(UserState.NotAuthorized)

            observer(notesRepo, apiService, userCenter, backgroundScope)
            advanceUntilIdle()

            // Sign in
            userCenter.authState.value = UserState.Authorized
            advanceUntilIdle() // initial emission consumed → dropped

            notesRepo.emit(listOf(note("id1")))
            advanceUntilIdle()

            assertEquals(listOf("id1"), apiService.createCalls)
        }
}
