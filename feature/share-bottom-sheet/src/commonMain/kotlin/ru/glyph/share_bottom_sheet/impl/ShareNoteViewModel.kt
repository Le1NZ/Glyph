package ru.glyph.share_bottom_sheet.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.glyph.model.NotePermission

internal class ShareNoteViewModel(
    private val noteId: String,
    private val repository: ShareNoteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ShareNoteUiState())
    val state: StateFlow<ShareNoteUiState> = _state.asStateFlow()

    init {
        loadShares()
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(emailInput = email, error = null) }
    }

    fun onAddShare() {
        val email = _state.value.emailInput.trim()
        if (email.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isAdding = true, error = null) }
            try {
                val newShare = repository.addShare(noteId, email, NotePermission.READ)
                _state.update { current ->
                    val newShares = current.shares.filter { it.email != email } + newShare
                    current.copy(shares = newShares, emailInput = "", isAdding = false)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isAdding = false, error = true) }
            }
        }
    }

    fun onUpdatePermission(email: String, permission: NotePermission) {
        viewModelScope.launch {
            try {
                val updated = repository.updateShare(noteId, email, permission)
                _state.update { current ->
                    val newShares = current.shares.map { if (it.email == email) updated else it }
                    current.copy(shares = newShares, error = null)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = true) }
            }
        }
    }

    fun onRemoveShare(email: String) {
        viewModelScope.launch {
            try {
                repository.removeShare(noteId, email)
                _state.update { current ->
                    current.copy(shares = current.shares.filter { it.email != email }, error = null)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = true) }
            }
        }
    }

    fun onRetry() {
        loadShares()
    }

    private fun loadShares() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val shares = repository.getShares(noteId)
                _state.update { it.copy(shares = shares, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = true) }
            }
        }
    }
}