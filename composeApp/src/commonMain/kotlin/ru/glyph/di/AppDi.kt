package ru.glyph.di

import ru.glyph.auth.api.di.AuthLocalDi
import ru.glyph.datastore.api.di.DataStoreLocalDi
import ru.glyph.home.HomeDi
import ru.glyph.navigation.NavigationDi
import ru.glyph.network.NetworkDi
import ru.glyph.network.api.di.NetworkLocalDi
import ru.glyph.screen.auth.api.di.AuthScreenDi
import ru.glyph.screen.profile.api.di.ProfileScreenDi

internal object AppDi {

    val modules = listOf(
        NavigationDi.module,
        DataStoreLocalDi.module,
        NetworkLocalDi.module,
        AuthLocalDi.module,
        NetworkDi.module,
        HomeDi.module,
        AuthScreenDi.module,
        ProfileScreenDi.module,
    )
}
