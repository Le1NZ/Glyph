package ru.glyph.screen.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.SignInResult
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.auth.ui.composable.model.AuthUiEffect
import ru.glyph.screen.auth.ui.composable.model.AuthUiState
import ru.glyph.sync.api.SyncBootstrap

internal class AuthScreenViewModel(
    private val userCenter: UserCenter,
    private val navigator: Navigator,
    private val syncBootstrap: SyncBootstrap,
) : ViewModel() {

    private val _effects = MutableSharedFlow<AuthUiEffect>()
    val effects = _effects.asSharedFlow()

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Ready)
    val state = _state.asStateFlow()

    fun onSignInClick() {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading

            when (userCenter.signIn()) {
                is SignInResult.Success -> {
                    syncBootstrap.pullAll()
                    navigator.navigateTo(screen = Screen.Home, clearBackStack = true)
                }

                SignInResult.Cancelled -> _state.value = AuthUiState.Ready

                is SignInResult.Error -> {
                    _state.value = AuthUiState.Ready
                    _effects.emit(AuthUiEffect.ErrorMessage)
                }
            }
        }
    }
}
