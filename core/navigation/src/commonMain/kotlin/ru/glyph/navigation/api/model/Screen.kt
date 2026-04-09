package ru.glyph.navigation.api.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : BaseDestination() {

    @Serializable
    data object Home : Screen()

    @Serializable
    data object Auth : Screen()

    @Serializable
    data object Profile : Screen()
}