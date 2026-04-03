package ru.glyph.di

import ru.glyph.home.HomeDi
import ru.glyph.navigation.NavigationDi
import ru.glyph.navigation.api.di.NavigationLocalDi

internal object AppDi {

    val modules = listOf(
        NavigationLocalDi.module,
        NavigationDi.module,
        HomeDi.module,
    )
}