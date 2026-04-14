package ru.glyph.server.database

import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import ru.glyph.server.model.CreateNoteRequest
import ru.glyph.server.model.UpdateNoteRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotesRepositoryTest {

    private val userId = "user-1"

    @BeforeTest
    fun setUp() {
        Database.connect(
            url = "jdbc:h2:mem:notesrepo;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction { SchemaUtils.create(Users, Notes) }
    }

    @AfterTest
    fun tearDown() {
        transaction { SchemaUtils.drop(Notes, Users) }
    }

    // ─── ensureUser ───────────────────────────────────────────────────────────

    @Test
    fun `ensureUser creates user on first call`() = runTest {
        NotesRepository.ensureUser(userId)
        // If user was created, getAll should not throw
        val notes = NotesRepository.getAll(userId)
        assertEquals(emptyList(), notes)
    }

    @Test
    fun `ensureUser is idempotent`() = runTest {
        NotesRepository.ensureUser(userId)
        NotesRepository.ensureUser(userId) // second call must not throw or duplicate
        assertEquals(0, NotesRepository.getAll(userId).size)
    }

    // ─── getAll ───────────────────────────────────────────────────────────────

    @Test
    fun `getAll returns empty list for new user`() = runTest {
        NotesRepository.ensureUser(userId)
        assertEquals(emptyList(), NotesRepository.getAll(userId))
    }

    @Test
    fun `getAll returns only notes belonging to requesting user`() = runTest {
        val otherUser = "user-2"
        NotesRepository.ensureUser(userId)
        NotesRepository.ensureUser(otherUser)

        NotesRepository.create(userId, CreateNoteRequest("n1", "T1", "C1", 100L, 200L))
        NotesRepository.create(otherUser, CreateNoteRequest("n2", "T2", "C2", 100L, 200L))

        val notes = NotesRepository.getAll(userId)
        assertEquals(1, notes.size)
        assertEquals("n1", notes[0].id)
    }

    @Test
    fun `getAll returns multiple notes ordered by updatedAt`() = runTest {
        NotesRepository.ensureUser(userId)
        NotesRepository.create(userId, CreateNoteRequest("n1", "First", "C1", 100L, 100L))
        NotesRepository.create(userId, CreateNoteRequest("n2", "Second", "C2", 100L, 200L))

        val notes = NotesRepository.getAll(userId)
        assertEquals(2, notes.size)
        // Exposed orderBy ASC by default → first note has lower updatedAt
        assertEquals("n1", notes[0].id)
        assertEquals("n2", notes[1].id)
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    fun `create stores note and returns dto with correct fields`() = runTest {
        NotesRepository.ensureUser(userId)
        val req = CreateNoteRequest(
            id = "note-xyz",
            title = "My Title",
            content = "My Content",
            createdAt = 1000L,
            updatedAt = 2000L,
        )
        val note = NotesRepository.create(userId, req)

        assertEquals("note-xyz", note.id)
        assertEquals("My Title", note.title)
        assertEquals("My Content", note.content)
        assertEquals(1000L, note.createdAt)
        assertEquals(2000L, note.updatedAt)
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    fun `getById returns note when it belongs to user`() = runTest {
        NotesRepository.ensureUser(userId)
        NotesRepository.create(userId, CreateNoteRequest("n1", "T", "C", 1L, 1L))

        val note = NotesRepository.getById("n1", userId)
        assertNotNull(note)
        assertEquals("n1", note.id)
    }

    @Test
    fun `getById returns null for note belonging to another user`() = runTest {
        val otherUser = "user-2"
        NotesRepository.ensureUser(userId)
        NotesRepository.ensureUser(otherUser)
        NotesRepository.create(userId, CreateNoteRequest("n1", "T", "C", 1L, 1L))

        val note = NotesRepository.getById("n1", otherUser)
        assertNull(note)
    }

    @Test
    fun `getById returns null for non-existent id`() = runTest {
        NotesRepository.ensureUser(userId)
        assertNull(NotesRepository.getById("does-not-exist", userId))
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    fun `update changes title, content and updatedAt`() = runTest {
        NotesRepository.ensureUser(userId)
        NotesRepository.create(userId, CreateNoteRequest("n1", "Old Title", "Old Content", 1L, 1L))

        val updated = NotesRepository.update("n1", userId, UpdateNoteRequest("New Title", "New Content", 999L))

        assertNotNull(updated)
        assertEquals("New Title", updated.title)
        assertEquals("New Content", updated.content)
        assertEquals(999L, updated.updatedAt)
    }

    @Test
    fun `update returns null when note belongs to another user`() = runTest {
        val otherUser = "user-2"
        NotesRepository.ensureUser(userId)
        NotesRepository.ensureUser(otherUser)
        NotesRepository.create(userId, CreateNoteRequest("n1", "T", "C", 1L, 1L))

        val result = NotesRepository.update("n1", otherUser, UpdateNoteRequest("X", "X", 1L))
        assertNull(result)
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    fun `delete removes note and returns true`() = runTest {
        NotesRepository.ensureUser(userId)
        NotesRepository.create(userId, CreateNoteRequest("n1", "T", "C", 1L, 1L))

        val deleted = NotesRepository.delete("n1", userId)

        assertTrue(deleted)
        assertNull(NotesRepository.getById("n1", userId))
    }

    @Test
    fun `delete returns false when note belongs to another user`() = runTest {
        val otherUser = "user-2"
        NotesRepository.ensureUser(userId)
        NotesRepository.ensureUser(otherUser)
        NotesRepository.create(userId, CreateNoteRequest("n1", "T", "C", 1L, 1L))

        val deleted = NotesRepository.delete("n1", otherUser)

        assertFalse(deleted)
        assertNotNull(NotesRepository.getById("n1", userId)) // still exists
    }
}
