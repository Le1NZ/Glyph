package ru.glyph.screen.home.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.glyph.navigation.api.Navigator
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.home.ui.composable.model.HomeUiState

internal class HomeScreenViewModel(
    private val navigator: Navigator,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _state = MutableStateFlow(HomeUiState.forPreview())
    val state = _state.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onProfileClick() {
        navigator.navigateTo(Screen.Profile)
    }
}
