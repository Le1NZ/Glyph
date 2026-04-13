package ru.glyph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.UserState
import ru.glyph.database.api.NotesRepository
import ru.glyph.sync.api.SyncBootstrap

internal class AppViewModel(
    userCenter: UserCenter,
    private val notesRepository: NotesRepository,
    private val syncBootstrap: SyncBootstrap,
) : ViewModel() {

    init {
        viewModelScope.launch {
            userCenter.authState
                .filter { it == UserState.NotAuthorized }
                .collect { notesRepository.deleteAll() }
        }
    }

    fun onForeground() {
        syncBootstrap.pullAsync()
    }
}
