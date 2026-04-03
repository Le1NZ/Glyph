package ru.glyph.navigation.ui

import androidx.lifecycle.ViewModel
import ru.glyph.navigation.impl.NavigatorImpl

internal class NavigatorScreenViewModel(
    private val navigatorImpl: NavigatorImpl,
) : ViewModel() {

    val navigationEvents = navigatorImpl.events

    fun onBackFromScreenDestination() {
        navigatorImpl.popBackStack()
    }
}
