package ru.glyph.screen.home.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.glyph.screen.home.ui.composable.model.HomeUiState

internal interface HomeScreenPresenter {

    val searchQuery: StateFlow<String>
    val state: StateFlow<HomeUiState>

    fun onProfileClick()
    fun onNoteClick(id: String)
    fun onCreateNoteClick()
    fun onSearchQueryChanged(query: String)
    fun onRefresh()
}

internal class HomeScreenPresenterImpl(
    private val viewModel: HomeScreenViewModel,
) : HomeScreenPresenter {

    override val searchQuery = viewModel.searchQuery
    override val state = viewModel.state

    override fun onProfileClick() = viewModel.onProfileClick()
    override fun onNoteClick(id: String) = viewModel.onNoteClick(id)
    override fun onCreateNoteClick() = viewModel.onCreateNoteClick()
    override fun onSearchQueryChanged(query: String) = viewModel.onSearchQueryChanged(query)
    override fun onRefresh() = viewModel.onRefresh()
}

internal class HomeScreenPresenterPreview : HomeScreenPresenter {

    override val searchQuery = MutableStateFlow("")
    override val state = MutableStateFlow(HomeUiState.forPreview())

    override fun onProfileClick() = Unit
    override fun onNoteClick(id: String) = Unit
    override fun onCreateNoteClick() = Unit
    override fun onSearchQueryChanged(query: String) = Unit
    override fun onRefresh() = Unit
}
