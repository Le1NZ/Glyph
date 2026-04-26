package ru.glyph.screen.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.UserInfoUseCase
import ru.glyph.auth.api.model.UserInfo
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.profile.ui.state.ProfileUiState
import ru.glyph.screen.profile.ui.state.UserUiModel
import ru.glyph.utils.ConvertedResult

internal class ProfileScreenViewModel(
    private val userCenter: UserCenter,
    private val userInfoUseCase: UserInfoUseCase,
    private val navigator: Navigator,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadUserInfo()
    }

    fun onSignOutClick() {
        viewModelScope.launch {
            userCenter.signOut()
            navigator.navigateTo(Screen.Auth, clearBackStack = true)
        }
    }

    fun onRetryClick() {
        _uiState.value = ProfileUiState.Loading
        loadUserInfo()
    }

    fun onBackClick() {
        navigator.popBackStack()
    }

    private fun loadUserInfo() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = when (val result = userInfoUseCase()) {
                is ConvertedResult.Success -> ProfileUiState.Success(
                    user = result.value.toUiModel(),
                )

                is ConvertedResult.Error -> ProfileUiState.Error
            }
        }
    }

    private fun UserInfo.toUiModel() = UserUiModel(
        initials = buildString {
            firstName?.firstOrNull()?.let(::append)
            lastName?.firstOrNull()?.let(::append)
        }.ifEmpty { displayName.take(1).uppercase() },
        displayName = displayName,
        email = email.takeIf { !it.isNullOrBlank() },
        avatarUrl = avatarUrl,
    )
}
