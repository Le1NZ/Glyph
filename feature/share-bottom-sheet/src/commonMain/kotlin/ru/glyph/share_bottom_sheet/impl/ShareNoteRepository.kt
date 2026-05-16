package ru.glyph.share_bottom_sheet.impl

import ru.glyph.model.NotePermission

internal interface ShareNoteRepository {
    suspend fun getShares(noteId: String): List<NoteShareDto>
    suspend fun addShare(noteId: String, email: String, permission: NotePermission): NoteShareDto
    suspend fun updateShare(noteId: String, email: String, permission: NotePermission): NoteShareDto
    suspend fun removeShare(noteId: String, email: String)
}

internal class ShareNoteRepositoryImpl(
    private val apiService: ShareApiService,
) : ShareNoteRepository {

    override suspend fun getShares(noteId: String): List<NoteShareDto> {
        return apiService.getShares(noteId)
    }

    override suspend fun addShare(noteId: String, email: String, permission: NotePermission): NoteShareDto {
        return apiService.addShare(noteId, email, permission)
    }

    override suspend fun updateShare(noteId: String, email: String, permission: NotePermission): NoteShareDto {
        return apiService.updateShare(noteId, email, permission)
    }

    override suspend fun removeShare(noteId: String, email: String) {
        apiService.removeShare(noteId, email)
    }
}