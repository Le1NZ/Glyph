package ru.glyph.navigation.ui

import kotlinx.coroutines.flow.SharedFlow
import ru.glyph.navigation.api.deps.InitialDestinationsProvider
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.ui.event.NavigatorEvent

internal interface NavigatorScreenPresenter {

    val events: SharedFlow<NavigatorEvent>

    fun getInitialDestinations(): List<BaseDestination>
    fun onBackFromScreenDestination()
}

internal class NavigatorScreenPresenterImpl(
    private val viewModel: NavigatorScreenViewModel,
) : NavigatorScreenPresenter {

    override val events = viewModel.navigationEvents

    override fun getInitialDestinations(): List<BaseDestination> {
        return viewModel.getInitialDestinations()
    }

    override fun onBackFromScreenDestination() {
        viewModel.onBackFromScreenDestination()
    }
}
