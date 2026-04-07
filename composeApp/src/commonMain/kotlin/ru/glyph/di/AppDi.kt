package ru.glyph.di

import ru.glyph.home.HomeDi
import ru.glyph.navigation.NavigationDi

internal object AppDi {

    val modules = listOf(
        NavigationDi.module,
        HomeDi.module,
    )
}