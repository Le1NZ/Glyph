package ru.glyph.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.SignInResult
import ru.glyph.auth.api.model.UserState
import ru.glyph.database.api.NotesRepository
import ru.glyph.database.api.entity.NoteEntity
import ru.glyph.sync.internal.SyncObserver
import ru.glyph.sync.internal.network.NoteApiService
import ru.glyph.sync.internal.network.dto.NoteDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncObserverTest {

    // ─── Fakes ───────────────────────────────────────────────────────────────

    private class FakeNotesRepository : NotesRepository {
        private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())

        val upsertCalls = mutableListOf<NoteEntity>()
        var deleteAllCallCount = 0

        override fun observeAll(): Flow<List<NoteEntity>> = _notes.asStateFlow()

        override suspend fun getById(id: String) = _notes.value.find { it.id == id }

        override suspend fun create(title: String, content: String): String {
            val id = "gen-${_notes.value.size}"
            _notes.value = _notes.value + NoteEntity(id, title, content, 0L, 0L)
            return id
        }

        override suspend fun upsert(
            id: String, title: String, content: String, createdAt: Long, updatedAt: Long,
        ) {
            val entity = NoteEntity(id, title, content, createdAt, updatedAt)
            upsertCalls.add(entity)
            val list = _notes.value.toMutableList()
            val idx = list.indexOfFirst { it.id == id }
            if (idx >= 0) list[idx] = entity else list.add(entity)
            _notes.value = list
        }

        override suspend fun update(id: String, title: String, content: String) {
            val list = _notes.value.toMutableList()
            val idx = list.indexOfFirst { it.id == id }
            if (idx >= 0) list[idx] = list[idx].copy(title = title, content = content)
            _notes.value = list
        }

        override suspend fun delete(id: String) {
            _notes.value = _notes.value.filter { it.id != id }
        }

        override suspend fun deleteAll() {
            deleteAllCallCount++
            _notes.value = emptyList()
        }

        fun emit(notes: List<NoteEntity>) {
            _notes.value = notes
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
            id: String, title: String, content: String, createdAt: Long, updatedAt: Long,
        ): NoteDto {
            createCalls.add(id)
            return NoteDto(id, title, content, createdAt, updatedAt)
        }

        override suspend fun update(id: String, title: String, content: String, updatedAt: Long): NoteDto {
            updateCalls.add(id)
            return NoteDto(id, title, content, 0L, updatedAt)
        }

        override suspend fun delete(id: String) {
            deleteCalls.add(id)
        }
    }

    private class FakeUserCenter(
        initialState: UserState = UserState.NotAuthorized,
    ) : UserCenter {
        override val authState = MutableStateFlow(initialState)
        override fun getToken() = if (authState.value == UserState.Authorized) "token" else null
        override suspend fun signIn() = SignInResult.Success
        override suspend fun signOut() { authState.value = UserState.NotAuthorized }
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private fun note(id: String, updatedAt: Long = 0L) =
        NoteEntity(id, "Title $id", "Content $id", 0L, updatedAt)

    private fun dto(id: String, updatedAt: Long = 0L) =
        NoteDto(id, "Title $id", "Content $id", 0L, updatedAt)

    // ─── pullAll tests ────────────────────────────────────────────────────────

    @Test
    fun `pullAll skips API call when not authorized`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.NotAuthorized)

        val observer = SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
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

        val observer = SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
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
            override suspend fun create(id: String, title: String, content: String, createdAt: Long, updatedAt: Long) =
                NoteDto(id, title, content, createdAt, updatedAt)
            override suspend fun update(id: String, title: String, content: String, updatedAt: Long) =
                NoteDto(id, title, content, 0L, updatedAt)
            override suspend fun delete(id: String) {}
        }
        val userCenter = FakeUserCenter(UserState.Authorized)

        val observer = SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
        val result = observer.pullAll()

        assertTrue(result.isFailure)
        assertEquals(0, notesRepo.deleteAllCallCount)
    }

    @Test
    fun `DB changes during pullAll are not pushed to server`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.Authorized)

        val observer = SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
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
    fun `observer does not push when user is not authorized`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.NotAuthorized)

        SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
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

        SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
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

        SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
        advanceUntilIdle() // consumes [note1] as initial, drops it

        // Remove the note
        notesRepo.emit(emptyList())
        advanceUntilIdle()

        assertEquals(listOf("id1"), apiService.deleteCalls)
        assertTrue(apiService.createCalls.isEmpty())
    }

    @Test
    fun `observer calls update API when note updatedAt changes`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.Authorized)

        notesRepo.emit(listOf(note("id1", updatedAt = 100L)))

        SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
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

        SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
        advanceUntilIdle()

        // Sign out
        userCenter.authState.value = UserState.NotAuthorized
        advanceUntilIdle()

        notesRepo.emit(listOf(note("id1")))
        advanceUntilIdle()

        assertTrue(apiService.createCalls.isEmpty(), "Should not push after sign out")
    }

    @Test
    fun `observer resumes pushing after user signs back in`() = runTest(UnconfinedTestDispatcher()) {
        val notesRepo = FakeNotesRepository()
        val apiService = FakeNoteApiService()
        val userCenter = FakeUserCenter(UserState.NotAuthorized)

        SyncObserver(notesRepo, apiService, userCenter, backgroundScope)
        advanceUntilIdle()

        // Sign in
        userCenter.authState.value = UserState.Authorized
        advanceUntilIdle() // initial emission consumed → dropped

        notesRepo.emit(listOf(note("id1")))
        advanceUntilIdle()

        assertEquals(listOf("id1"), apiService.createCalls)
    }
}
