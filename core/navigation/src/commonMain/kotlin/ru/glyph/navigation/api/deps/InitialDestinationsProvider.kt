package ru.glyph.navigation.api.deps

import ru.glyph.navigation.api.model.BaseDestination

interface InitialDestinationsProvider {

    fun initialDestinations(): List<BaseDestination>
}
